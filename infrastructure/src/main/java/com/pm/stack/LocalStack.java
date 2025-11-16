package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack {
  private final Vpc vpc;
  private final Cluster ecsCluster;

  public LocalStack(final App scope, final String id, StackProps props) {
    super(scope, id, props);

    this.vpc = createVpc();

    DatabaseInstance catalogServiceDb =
            createDatabase("catalogServiceDb", "catalog-service-db");
    CfnHealthCheck catalogDbHealthCheck = createDbHealthCheck(catalogServiceDb, "catalogServiceDbHealthCheck");

    CfnCluster mskCluster = createMskCluster();

    this.ecsCluster = createEcsCluster();

    FargateService eurekaService =
            createFargateService("EurekaService",
                    "eureka-service",
                    List.of(8761),
                    null,
                    Map.of("EUREKA_CLIENT_FETCHREGISTRY", "false",
                            "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE","http://eureka-service.insighttune.local:8761/eureka/",
                            "EUREKA_CLIENT_REGISTERWITHEUREKA", "false"));
    FargateService catalogService =
            createFargateService("CatalogService",
                    "catalog-service",
                    List.of(4000),
                    catalogServiceDb,
                    null);
    FargateService historyService =
            createFargateService("HistoryService",
                    "history-service",
                    List.of(4003),
                    null, null);

    FargateService playingService =
            createFargateService("PlayingService",
                    "playing-service",
                    List.of(4001),
                    null, null);

    FargateService favoriteService =
            createFargateService("FavoriteService",
                    "favorite-service",
                    List.of(4004),
                    null, null);

    FargateService recsysService =
            createFargateService("RecSysService",
                    "rec-sys",
                    List.of(4007),
                    null, null);

    FargateService recommendService =
            createFargateService("RecommendService",
                    "recommend-service",
                    List.of(4008),
                    null, null);
    FargateService authService =
            createFargateService("AuthService",
                    "auth-service",
                    List.of(4006),
                    null, null);
    FargateService userService =
            createFargateService("UserService",
                    "user-service",
                    List.of(4005),
                    null, null);
    catalogService.getNode().addDependency(catalogDbHealthCheck);
    catalogService.getNode().addDependency(eurekaService);
    catalogService.getNode().addDependency(catalogServiceDb);
    catalogService.getNode().addDependency(mskCluster);

//    recommendService.getNode().addDependency(historyService);
//    recommendService.getNode().addDependency(catalogService);
    historyService.getNode().addDependency(mskCluster);

    ApplicationLoadBalancedFargateService apiGateway = createApiGatewayService();
    apiGateway.getNode().addDependency(eurekaService);
    apiGateway.getNode().addDependency(catalogService);
    apiGateway.getNode().addDependency(historyService);
    apiGateway.getNode().addDependency(playingService);
    apiGateway.getNode().addDependency(favoriteService);
//    apiGateway.getNode().addDependency(recommendService);
  }

  private Vpc createVpc() {
    return Vpc.Builder.create(this, "InsightTuneVPC").vpcName("InsightTuneVPC")
            .maxAzs(2)
            .build();
  }

  private DatabaseInstance createDatabase(String id, String name) {
    return DatabaseInstance.Builder
            .create(this, id).engine(DatabaseInstanceEngine.postgres(
                    PostgresInstanceEngineProps.builder()
                            .version(PostgresEngineVersion.VER_17_2)
                            .build()))
            .vpc(vpc)
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
            .allocatedStorage(20)
            .credentials(Credentials.fromGeneratedSecret("admin"))
            .databaseName(name)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();
  }

  private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
    return CfnHealthCheck.Builder.create(this, id)
            .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                    .type("TCP")
                    .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                    .ipAddress(db.getDbInstanceEndpointAddress())
                    .requestInterval(30)
                    .failureThreshold(3)
                    .build()
            )
            .build();
  }

  private CfnCluster createMskCluster() {
    return CfnCluster.Builder.create(this, "MskCluster")
            .clusterName("kafka-cluster")
            .kafkaVersion("3.6.0")
            .numberOfBrokerNodes(2)
            .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                    .instanceType("kafka.m5.xlarge")
                    .clientSubnets(vpc.getPrivateSubnets().stream()
                            .map(ISubnet::getSubnetId)
                            .collect(Collectors.toList()))
                    .brokerAzDistribution("DEFAULT")
                    .build())
            .build();
  }

  private Cluster createEcsCluster() {
    return Cluster.Builder.create(this, "InsightTuneCluster")
            .vpc(vpc)
            .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                    .name("insighttune.local").build())
            .build();
  }

  private FargateService createFargateService(String id, String imageName, List<Integer> ports,
                                              DatabaseInstance db, Map<String, String> additionalEnvVars) {
    FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
            .cpu(256)
            .memoryLimitMiB(512)
            .build();

    ContainerDefinitionOptions.Builder containerDefinitionOptionsBuilder =
            ContainerDefinitionOptions.builder()
                    .image(ContainerImage.fromRegistry(imageName))
                    .portMappings(ports.stream()
                            .map(port -> PortMapping.builder()
                                    .containerPort(port)
                                    .hostPort(port)
                                    .protocol(Protocol.TCP)
                                    .build()).toList())
                    .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                            .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                    .logGroupName("/ecs/" + imageName)
                                    .removalPolicy(RemovalPolicy.DESTROY)
                                    .retention(RetentionDays.ONE_DAY)
                                    .build())
                            .streamPrefix(imageName)
                            .build()));
    Map<String, String> envVars = new HashMap<>();
    envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, " +
            "localhost.localstack.cloud:4511, " +
            "localhost.localstack.cloud:4512");
    envVars.put(
            "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
            "http://eureka-service.insighttune.local:8761/eureka/"
    );
    if (additionalEnvVars != null) {
      envVars.putAll(additionalEnvVars);
    }
    if (db != null) {
      envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
              db.getDbInstanceEndpointAddress(),
              db.getDbInstanceEndpointPort(),
              imageName
      ));
      envVars.put("SPRING_DATASOURCE_USERNAME", "admin");
      envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
      envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
      envVars.put("SPRING_SQL_INIT_MODE", "always");
      envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "1000");
    }
    containerDefinitionOptionsBuilder.environment(envVars);
    taskDefinition.addContainer(imageName + "Container", containerDefinitionOptionsBuilder.build());

    return FargateService.Builder.create(this, id)
            .cluster(ecsCluster)
            .taskDefinition(taskDefinition)
            .assignPublicIp(false)
            .serviceName(imageName)
            .cloudMapOptions(CloudMapOptions.builder()
                    .name(imageName)
                    .dnsRecordType(DnsRecordType.A)
                    .build())
            .build();
  }

  private ApplicationLoadBalancedFargateService createApiGatewayService() {
    FargateTaskDefinition taskDefinition =
            FargateTaskDefinition.Builder.create(this, "APIGatewayTaskDefinition")
                    .cpu(256)
                    .memoryLimitMiB(512)
                    .build();

    Map<String, String> envVars = new HashMap<>();
    envVars.put(
            "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
            "http://eureka-service.insighttune.local:8761/eureka/"
    );


    ContainerDefinitionOptions containerOptions =
            ContainerDefinitionOptions.builder()
                    .image(ContainerImage.fromRegistry("api-gateway"))
                    .environment(envVars)
                    .portMappings(List.of(4010).stream()
                            .map(port -> PortMapping.builder()
                                    .containerPort(port)
                                    .hostPort(port)
                                    .protocol(Protocol.TCP)
                                    .build())
                            .toList())
                    .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                            .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                    .logGroupName("/ecs/api-gateway")
                                    .removalPolicy(RemovalPolicy.DESTROY)
                                    .retention(RetentionDays.ONE_DAY)
                                    .build())
                            .streamPrefix("api-gateway")
                            .build()))
                    .build();

    taskDefinition.addContainer("APIGatewayContainer", containerOptions);

    return ApplicationLoadBalancedFargateService.Builder.create(this, "APIGatewayService")
            .cluster(ecsCluster)
            .serviceName("api-gateway")
            .taskDefinition(taskDefinition)
            .desiredCount(1)
            .healthCheckGracePeriod(Duration.seconds(60))
            .build();
  }


  public static void main(final String[] args) {
    App app = new App(AppProps.builder().outdir("./cdk.out").build());
    StackProps props = StackProps.builder()
            .synthesizer(new BootstraplessSynthesizer())
            .build();

    new LocalStack(app, "localstack", props);
    app.synth();
    System.out.println("App synthesizing in progress ...");
  }
}
