FROM maven:3-eclipse-temurin-20

### Time Zone ###
ENV TZ Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

### Maven ###
ENV M2_REPO /home/gitpod/.m2/repository/

### Git ###
RUN apt update && apt install -y git
