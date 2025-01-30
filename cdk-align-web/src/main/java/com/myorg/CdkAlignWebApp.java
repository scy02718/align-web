package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CdkAlignWebApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkAlignWebStack(app, "CdkAlignWebStack");

        app.synth();
    }
}

