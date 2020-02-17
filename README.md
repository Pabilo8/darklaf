# Darklaf - A Darcula Look and Feel

[![CI Status](https://github.com/weisJ/darklaf/workflows/CI/badge.svg?branch=master)](https://github.com/weisJ/darklaf/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.weisj/darklaf-core?label=Maven%20Central)](https://search.maven.org/artifact/com.github.weisj/darklaf-core)

This project is based on the [darcula](https://github.com/bulenkov/Darcula) look and feel for Swing.

# Screenshots
![Darcula Theme](https://github.com/weisJ/darklaf/blob/master/img/file_chooser_darcula.png) | ![IntelliJ Theme](https://github.com/weisJ/darklaf/blob/master/img/file_chooser_intellij.png)
:-------------------------:|:-------------------------:
Darcula Theme | IntelliJ Theme

![Solarized Dark Theme](https://github.com/weisJ/darklaf/blob/master/img/file_chooser_solarized_dark.png) | ![Solarized Light Theme](https://github.com/weisJ/darklaf/blob/master/img/file_chooser_solarized_light.png)
:-------------------------:|:-------------------------:
Solarized Dark Theme | Solarized Light Theme

# Usage & [Features](https://github.com/weisJ/darklaf/wiki/Features)
A list of all features can be found [here](https://github.com/weisJ/darklaf/wiki/Features).
The LaF is compatible with Java >=1.8 (you need >=1.9 to get proper scaling).
This project is available on Maven Central:
### Maven
````xml
<dependency>
  <groupId>com.github.weisj</groupId>
  <artifactId>darklaf</artifactId>
  <version>[1.3.3.5,)</version>
</dependency>
````
### Gradle
````
implementation 'com.github.weisj:darklaf-core:[1.3.3.5,)'
````

## Installation
The LookAndFeel can be easily installed using the 'LafManager'
````java
LafManager.install(); //For default theme (IntelliJ)
LafManager.install(new DarculaTheme()); //Specify the used theme.
````
or by using the UIManager
````java
LafManager.setTheme(new DarculaTheme());
UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
````

## Example
````java
import com.github.weisj.darklaf.LafManager;
import javax.swing.*;
import java.awt.*;

public class DarklafDemo {

    public static void main(final String[] args) {
         SwingUtilities.invokeLater(() -> {
            LafManager.install();

            JFrame frame = new JFrame("Darklaf - A themeable LaF for Swing");
            frame.setSize(600, 400);

            JButton button = new JButton("Click here!");

            JPanel content = new JPanel();
            content.add(button);

            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(content);
            frame.setVisible(true);
        });
    }
}
````

All custom component-variants and settings can be enabled by setting the appropriate client
property of the component.
They can be found [here](https://github.com/weisJ/darklaf/wiki/Features#alternative-visualsbehaviour-for-components).

This LookAndFeel supports custom window decorations (only on Windows at this point). In contrast to most other LaFs the native window behaviour is not compromised giving a seamless experience that still looks native.

# Build

    ./gradlew build

When building on Windows, you need a C++ toolchain installed to build native extension.

# Contribute
Here is a list of things that currently are not finished or need refinement. This list is a work in progress and being updated regulary. If you find any problems with the LaF feel free to submit an issue:

- [Native window decorations on Linux and MacOS.](https://github.com/weisJ/darklaf/issues/2)
  * See [jniplatform/cpp](https://github.com/weisJ/darklaf/tree/master/src/jniplatform/cpp) for the windows implementation.
- [Adjustment of platform specific properties](https://github.com/weisJ/darklaf/issues/2)
  * See the [properties folder](https://github.com/weisJ/darklaf/tree/master/src/main/resources/com/github/weisj/darklaf/properties) especially the plaform folder.
- Create missing demo classes. (See Todo's in test/ui/)

# License
This project is licensed under the [MIT license](https://github.com/weisJ/darklaf/blob/master/LICENSE).

