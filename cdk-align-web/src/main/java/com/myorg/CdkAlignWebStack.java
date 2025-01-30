package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;
import software.constructs.Construct;
import software.amazon.awscdk.Environment;

public class CdkAlignWebStack extends Stack {
    public CdkAlignWebStack(final Construct scope, final String id) {
        super(scope, id);

        // Create a minimal-cost VPC (2) AZ)
        Vpc vpc = Vpc.Builder.create(this, "AlignWebVpc")
                .maxAzs(2) // Use only 2 Availability Zone to minimize costs. 1 Led to error as ALB requires 2 AZs
                .build();

        // Reference Existing ECR Repository
        IRepository ecrRepo =  Repository.fromRepositoryName(this, "AlignWebEcrRepo", "align-web");

        // Create an ECS Cluster (Serverless)
        Cluster cluster = Cluster.Builder.create(this, "AlignWebCluster")
                .vpc(vpc)
                .build();

        // Create a Fargate Service with ALB
        ApplicationLoadBalancedFargateService fargateService =
                ApplicationLoadBalancedFargateService.Builder.create(this, "AlignWebFargateService")
                        .cluster(cluster)
                        .cpu(256) // Minimal cost (0.25 vCPU)
                        .memoryLimitMiB(512) // Minimal memory
                        .desiredCount(1) // Only 1 Task Running
                        .publicLoadBalancer(true) // Expose via ALB
                        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(ecrRepo, System.getenv("CDK_CONTEXT_IMAGE_TAG")))
                                .containerPort(3000)
                                .build())
                        .build();
    }
}
