# file-manager

A file manager written in Clojure that aims to be portable yet powerful, and efficient.

Uses the Swing toolkit for graphical client.

## Installation

First, clone the repo. Then to run the program, you can use one of the following commands:

#### Compiling
```
$ lein uberjar
```

Alternatively to the above command, the jar file can be downloaded from the "Releases" section.

## Usage

#### Running from command line
```
$ lein run
```

#### Running .jar file

To run a compiled .jar file, use the following command:

```
$ java -jar file-manager-0.0.1-standalone.jar
```

If you compile the program, this will be stored in target/uberjar.

## Timeline

- [x] Implement file backend/interface with basic manipulation functionality
- [ ] Implement databases for tagged images/sorts
- [x] Implement basic image viewing using java.swing toolkit
- [x] Implement geolocation
- [ ] Implement OpenCV face/image identification and automated tagging

## License

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
