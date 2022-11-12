# Knight
Chess Social Network,
[ui](https://github.com/HeadcrabJ/knight-ui)

## Instruction
1. Clone this repository.
```shell
git clone https://github.com/HeadcrabJ/knight.git
```
2. Create project in Firebase.
3. Click to Settings icon and choose `Project settings`.
4. Click to tab `Service accounts`, choose `Firebase Admin SDK` and click button `Generate new private key`.
5. Rename `.env.example` to `.env` and write to this file required data (Don't forget specify path to the Firebase Admin SDK file). <br>
#### Build and run with Java
7. Install OpenJDK 17.
```shell
apt install openjdk-17-jdk
```
7. Build JAR.
```shell
./gradlew shadowJar
```
9. Run JAR.
```shell
java -jar build/libs/knight.jar
```
#### Build and run with Docker
7. Install Docker.
```shell
apt install docker
```
8. Build image.
```shell
docker build -t knight .
```
9. Run container.
```shell
docker run -d -p 8787:8787 knight
```

## Developed by
[u032](https://github.com/orchst), [Daniel](https://github.com/thedanielj)
