# RxJavaAndroidInterop
Interop library for exposing Android's main thread and loopers as RxJava 3 Schedulers directly.

Ported from https://github.com/ReactiveX/RxAndroid

<a href='https://github.com/akarnokd/RxJavaAndroidInterop/actions?query=workflow%3A%22Java+CI+with+Gradle%22'><img src='https://github.com/akarnokd/RxJavaAndroidInterop/workflows/Java%20CI%20with%20Gradle/badge.svg'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJavaAndroidInterop/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJavaAndroidInterop?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-android-interop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-android-interop)

RxJava 3: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava3/rxjava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava3/rxjava)


```groovy

dependencies {
    implementation "com.github.akarnokd:rxjava3-android-interop:3.0.0-RC6"
}
```

### Usage

```java

import hu.akarnokd.rxjava3.android.*;
import io.reactivex.rxjava3.core.*;

Flowable.intervalRange(1, 10, 1, 1, TimeUnit.SECONDS, AndroidInteropSchedulers.mainThread())
.subscribe(System.out::println);
```

#### plugins for overrides

```java
import hu.akarnokd.rxjava3.android.*;

AndroidInteropPlugins.setMainThreadSchedulerHandler(scheduler -> scheduler);
```

