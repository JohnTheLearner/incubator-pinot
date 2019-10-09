# Build ThirdEye Docker Image

Build a demo image for the [Pinot/ThirdEye](../README.md) project.

The constructed image contains all dependencies to bring-up ThirdEye and run the frontend in demo mode.  The [Dockerfile](Dockerfile) can be modified for other uses, but these instructions show building an image for easy and reproducible ThirdEye evaluation, development, and testing.


## Build image:  

* Clone the repo locally if you don't already have it.

`git clone git@github.com:apache/incubator-pinot.git`

* From the project root, navigate to the ThirdEye docker directory.

`cd thirdeye/docker/`

* Build a local docker image.
Note that the first run of this will likely take some time as other images and dependencies might need to be downloaded.

`docker build --tag=thirdeye .`


## Run container

Run a ThirdEye container using the below-specified [ephemeral ports](https://en.wikipedia.org/wiki/Ephemeral_port).  This runs the UI and the admin portals from container ports `1426` and `1427` and provides to the host system on ports `51426` and `51427`, respectively.  Host system ports can be changed to user needs, but the application ports are configured in the ThirdEye project.

`docker run -it -p 51426:1426 -p 51427:1427 thirdeye` 

Using the [quick start](https://thirdeye.readthedocs.io/en/latest/quick_start.html) as a guide, access the ThirdEye UI via the mapped ephemeral ports above via `http://localhost:51426/app/#/rootcause?metricId=1`
