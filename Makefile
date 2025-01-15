deps:
	./gradlew :instead:downloadDependencies

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

install:
	./gradlew installDebug

all: deps build install