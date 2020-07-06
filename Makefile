.DEFAULT_GOAL := build-release-run

build-release-run: build release-run

clean: 
	rm -rf ./target

build:
	mvn clean package

release-run: build
	./release.sh

install:
	mvn clean package install
