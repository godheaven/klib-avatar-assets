<p style="text-align:left">
  <img src="https://www.kanopus.cl/assets/kanopus_black.png" width="220" alt="Kanopus logo"/>
</p>

![Maven](https://img.shields.io/maven-central/v/cl.kanopus.util/klib-avatar-assets) ![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue) ![Java](https://img.shields.io/badge/java-21+-orange)

# klib-avatar-assets

This library is part of the Kanopus ecosystem and provides a curated collection of high-quality avatar assets (images) designed for seamless integration into offline and on-premise applications.

## ✨ Features

- **High-Quality Icons**: Hand-picked, premium avatar icons.
- **Optimized for Avatars**: square PNG files (between 180x180 and 324x324 px) with white silhouettes and transparent backgrounds, ready for custom background styling.
- **Resource Bundle**: Easily accessible through the classpath as a lightweight resource JAR.
- **Eco-system Ready**: Designed to work perfectly with other Kanopus libraries or as a standalone asset collection.

## 🚀 Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>cl.kanopus.util</groupId>
    <artifactId>klib-avatar-assets</artifactId>
    <version>4.20.0</version>
</dependency>
```

## 🚀 Usage Guide

The images are located in the classpath under `kanopus/avatars/`. You can load them directly using your preferred resource loading mechanism.

### Java API (since 4.20.0)

```java
AvatarCatalog catalog = AvatarCatalog.load();

catalog.categories();                         // [animals, aztecs, celts, ...]
String path = catalog.pick("user-42");        // stable avatar for a user id / e-mail
String viking = catalog.pick("user-42", "vikings");
byte[] png = catalog.readBytes(path);
try (InputStream in = catalog.open(path)) { /* stream it */ }
```

Only paths listed in `index.txt` can be opened.

### Resource Structure
- `kanopus/avatars/index.txt`: one classpath path per line for every bundled avatar.
- `kanopus/avatars/[category]/`: Directories containing the PNG assets.

## 👤 Author

⭐**Pablo Andrés Díaz Saavedra** — Founder of **Kanopus – Software Guided by the Stars**⭐

Kanopus is building a constellation of developers creating tools, libraries and platforms that simplify software engineering.

[GitHub](https://github.com/godheaven) | [LinkedIn](https://www.linkedin.com/in/pablo-diaz-saavedra-4b7b0522/) | [Website](https://kanopus.cl)

## 📄 License

This software is licensed under the Apache License, Version 2.0. See the LICENSE file for details.

[![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/license/apache-2-0)

## 🛟 Support

For support or questions contact: 📧 [soporte@kanopus.cl](mailto:soporte@kanopus.cl)
