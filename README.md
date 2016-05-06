# jTemper

[![Release](https://jitpack.io/v/User/Repo.svg)](https://jitpack.io/#thombohlk/jTemper)

jTemper is a small library to retrieve data from your TEMPer devices. Currently only the TEMPer2 device is supported. jTemper can be used standalone or as a library for your Java program. 

### Standalone
To build a runnable jar execute the following command inside the base directory:
```
$ gradle build
```
To retrieve the current device readouts run
```
$ sudo java -jar builds/libs/jtemper-0.1.jar
```

### Library usage
Include the following lines in your gradle.build file:
```
repositories {
    ...
    maven { url "https://jitpack.io"  }
}

dependencies {
    ...
    compile 'com.github.thombohlk:jTemper:Tag'
}
```

