FROM maven:3.8.2-openjdk-17-slim

### Time Zone ###
ENV TZ Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

### Git ###
RUN apt update && apt install -y git
