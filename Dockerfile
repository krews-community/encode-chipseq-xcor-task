# FROM openjdk:8-jre-slim as base
FROM ubuntu:16.04 as base
RUN apt-get update && apt-get install -y \
    openjdk-8-jre \
     libncurses5-dev \
        libncursesw5-dev \
        libcurl4-openssl-dev \
        libfreetype6-dev \
        zlib1g-dev \
        python \
        python-setuptools \
        python-pip \
        python3 \
        python3-setuptools \
        python3-pip \
        git \
        wget \
        unzip \
        ghostscript \
        pkg-config \
        libboost-dev \
        r-base-core \
        bash \
        apt-transport-https \
    tabix

RUN git clone --branch 1.2 --single-branch https://github.com/samtools/samtools.git && \
    git clone --branch 1.2 --single-branch https://github.com/samtools/htslib.git && \
    cd samtools && make && make install && cd ../ && rm -rf samtools* htslib*

RUN echo "r <- getOption('repos'); r['CRAN'] <- 'http://cran.r-project.org'; options(repos = r);" > ~/.Rprofile && \
    Rscript -e "install.packages('snow')" && \
    Rscript -e "install.packages('snowfall')" && \
    Rscript -e "install.packages('bitops')" && \
    Rscript -e "install.packages('caTools')" && \
    Rscript -e "source('http://bioconductor.org/biocLite.R'); biocLite('Rsamtools')"

# Install R package spp 1.14 (required for phantompeakqualtools)
RUN wget https://github.com/hms-dbmi/spp/archive/1.13.tar.gz && Rscript -e "install.packages('./1.13.tar.gz')" && rm -f 1.13.tar.gz

# Install phantompeakqualtools 1.2
RUN wget https://github.com/kundajelab/phantompeakqualtools/archive/1.2.tar.gz && tar -xvf 1.2.tar.gz && rm -f 1.2.tar.gz &&  cp phantompeakqualtools-1.2/run_spp.R /

ENV PATH="/software/phantompeakqualtools-1.2:${PATH}"

FROM openjdk:8-jdk-alpine as build
COPY . /src
WORKDIR /src

RUN ./gradlew clean shadowJar

FROM base
RUN mkdir /app
COPY --from=build /src/build/chipseq-xcor-*.jar /app/chipseq.jar