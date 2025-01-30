package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.cognito.PasswordPolicy;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableEncryption;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.s3.StorageClass;
import software.amazon.awscdk.services.s3.Transition;
import software.constructs.Construct;

import java.util.List;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Environment;

public class CdkAlignWebStack extends Stack {
    public CdkAlignWebStack(final Construct scope, final String id) {
        super(scope, id);

        // Create a DynamoDB Table for storing patient data
        Table dynamoDBTable = Table.Builder.create(this, "AlignDynamoDBTable")
                .tableName("align-patient-data")
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .partitionKey(Attribute.builder()
                        .name("PatientID")
                        .type(AttributeType.STRING)
                        .build())
                .encryption(TableEncryption.AWS_MANAGED)
                .pointInTimeRecovery(true)
                .build();

        // Create an S3 Bucket for storing chat data
        Bucket s3Bucket = Bucket.Builder.create(this, "AlignS3Bucket")
                .bucketName("align-chat-data")
                .versioned(true)
                .lifecycleRules(List.of(LifecycleRule.builder()
                        .id("ArchiveOldData")
                        .transitions(List.of(Transition.builder()
                                .transitionAfter(Duration.days(90))
                                .storageClass(StorageClass.GLACIER)
                                .build()))
                        .build()))
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .build();

        // Cognito User Pool for strong user authentication
        UserPool cognitoUserPool = UserPool.Builder.create(this, "AlignCognitoUserPool")
                .userPoolName("AlignUserPool")
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(12)
                        .requireLowercase(true)
                        .requireUppercase(true)
                        .requireDigits(true)
                        .requireSymbols(true)
                        .build())
                .standardAttributes(StandardAttributes.builder()
                        .email(StandardAttribute.builder().required(true).mutable(false).build())
                        .preferredUsername(StandardAttribute.builder().required(false).mutable(true).build())
                        .build())
                .build();

        // Create a minimal-cost VPC (2) AZ)
        Vpc vpc = Vpc.Builder.create(this, "AlignWebVpc")
                .maxAzs(2) // Use only 2 Availability Zone to minimize costs. 1 Led to error as ALB requires 2 AZs
                .build();

        // Reference Existing ECR Repository
        IRepository ecrRepo =  Repository.fromRepositoryName(this, "AlignWebEcrRepo", "align-web");

        // Create an ECS Cluster (Serverless)
        Cluster cluster = Cluster.Builder.create(this, "AlignWebCluster")
                .clusterName("AlignWebCluster")
                .vpc(vpc)
                .build();

        // Create a Fargate Service with ALB
        ApplicationLoadBalancedFargateService fargateService =
                ApplicationLoadBalancedFargateService.Builder.create(this, "AlignWebFargateService")
                        .serviceName("AlignWebFargateService")  
                        .cluster(cluster)
                        .cpu(256) // Minimal cost (0.25 vCPU)
                        .memoryLimitMiB(512) // Minimal memory
                        .desiredCount(1) // Only 1 Task Running
                        .publicLoadBalancer(true) // Expose via ALB
                        .minHealthyPercent(50)
                        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(ecrRepo, "latest"))
                                .containerPort(3000)
                                .build())
                        .build();
    }
}
