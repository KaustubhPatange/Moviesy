# After

[![Jitpack](https://jitpack.io/v/KaustubhPatange/Moviesy.svg)](https://jitpack.io/#KaustubhPatange/Moviesy)

**After** is a library that helps you to perform some task "after" some time one of which is displaying prompt.

> Library also contains some methods like displaying prompt and do something after close.

> This library is small & is used in Moviesy project. Once it becomes solid I'll create a separate repository this project.

<img height="500px" src="art/screen.gif"/>

## Download

In your top-level build.gradle file,

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

In your app/module build.gradle file add the following dependency.

```gradle
dependencies {
    ...
    implementation 'com.github.KaustubhPatange.Moviesy:after:tag'

    // For non Kotlin project
    def kotlin_version = "1.3.72"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
}
```

## Usage

```java
// Kotlin
After.time(10, TimeUnit.SECONDS).prompt(context, "Hello")

// Java
After.INSTANCE.time(1, TimeUnit.SECONDS).prompt(context, "Hello", new After.Options(), null);
```

### Additional configs (Optional)

- Add this in your _Application_ class.

```kotlin
After.Config.setTypeface(typeface) // Set custom font for text
            .setTextSize(14) // Set custom text size
```

### Additional Options

- You can also set some additional options to `prompt` method

```kotlin
// Kotlin
val options = After.Options()
After.time(10, TimeUnit.SECONDS).prompt(context, "Hello", options) {
    // Do something on prompt close
}

// Java
After.Options options = new After.Options();
After.INSTANCE.time(10, TimeUnit.SECONDS).prompt(MainActivity.this, "Hello", new After.Options(), new Function0<Unit>() {
        @Override
        public Unit invoke() {
            // Do something on prompt close.
            return null;
        }
    });
```

- You can also configure `After.Options`

```kotlin
// Kotlin (using named/typed arguments)
val options = After.Options(
     displayLocation = After.Location.TOP,
     emoji = After.Emoji.HAPPY
     // Omit them which you don't need
)
```

| Parameters        | Tasks                                                                      |
| ----------------- | -------------------------------------------------------------------------- |
| `displayLocation` | Location where this prompt should be displayed, default `Location.BOTTOM`. |
| `showIcon`        | Set if the icon should be visible or not, default `true`.                  |
| `emoji`           | Set the default emoji icon, default `Emoji.SAD`.                           |
| `drawableRes`     | Set the drawable icon instead of built-in emoji.                           |
| `textColor`       | Set the text color for the message.                                        |
| `backgroundColor` | Set the background color for the prompt, default `Color.Orange`.           |
| `progressColor`   | Set the progress color for the prompt, default `Color.red`.                |

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
