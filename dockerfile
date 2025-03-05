FROM tomcat:9.0.98-jdk17

# 기존 웹앱 제거
RUN rm -rf /usr/local/tomcat/webapps/*

# 프로젝트 WAR 파일 추가 (blindtime context path 설정)
COPY target/*.war /usr/local/tomcat/webapps/ROOT.war

# 포트 노출
EXPOSE 8080

# 톰캣 실행
CMD ["catalina.sh", "run"]
