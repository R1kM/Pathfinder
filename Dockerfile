FROM ubuntu:latest
MAINTAINER Aymeric Fromherz from version from Willem Visser, base version from Ivan Krizsan, https://github.com/krizsan
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer 
RUN apt-get install -y mercurial meld && \
    apt-get install -y ant && \
    apt-get install -y junit4 && \
    apt-get clean 
RUN apt-get install -y vim && \
    apt-get install -y git && \
    apt-get install -y subversion 
RUN git clone https://github.com/R1kM/Pathfinder 
WORKDIR /Pathfinder/jpf-core
ENV JUNIT_HOME /usr/share/java
RUN ant build 
RUN java -jar build/RunJPF.jar src/examples/Racer.jpf
WORKDIR /
RUN git clone https://github.com/Z3Prover/z3.git
RUN apt-get update && \
    apt-get install -y build-essential && \
    apt-get install -y gcc && \
    apt-get install -y m4 && \
    apt-get install -y g++
WORKDIR /z3
RUN python scripts/mk_make.py
WORKDIR /z3/build
RUN make && \
    make install
WORKDIR /Pathfinder/jpf-symbc
RUN ant build
RUN java -jar ../jpf-core/build/RunJPF.jar src/examples/TestPaths.jpf
WORKDIR /
COPY site.properties /root/.jpf/
ENV LD_LIBRARY_PATH /Pathfinder/jpf-symbc/lib
