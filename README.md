# Mobile Sensing Android

**Background**

An Android application that gathers and stores mobile sensor data, for use in a mobile sensing study. The study seeks to discover whether we can infer relationship closeness between two individuals walking together based on data gathered from mobile sensors.

The study has three phases: application design, data gathering, and data analysis.

**Application Design**

This repository represents the output of the first phase of the study. The master branch comprises the minimum viable product for the purpose of this study, which is now complete subject to further testing.

The application presents the user with a simple toggle switch to turn on or off mobile sensing. When active, the application reads data from a variety of the phone's mobile sensors and stores them, either to external storage or internally if no external option is available. This is achieved with the [SensingKit-Android](https://github.com/SensingKit/SensingKit-Android) library.

The following sensors are supported:

- Accelerometer
- Battery
- Gravity
- Gyroscope
- Linear Acceleration
- Magenetometer
- Rotation

If the toggle is left active, sensor data will continue to be read and saved if the application is left running in the background and if it is manually closed by the user (unless the process is killed by the OS to free up system memory). This is communicated to the user by a persistent notification.

Once the sensing session is complete, the data can be accessed simply by transfer to a PC over USB.

**Data Gathering**

The data gathering phases consists of recruiting participants to take part in a brief experiment in pairs. 

Participants will fill out a questionnaire based on the Unidimensional Relationship Closeness Scale to establish their closeness score. They will then converse and walk in pairs for 10 minutes with the application running in their front trouser pocket gathering data from the sensors listed above. The project facilitator will record this process to establish ground truth for later data interpretation. 

The study aims to gather data for at least 20 pairs of participants, 10 with some existing relationship and 10 without.

**Data Analysis**

Data analysis will consist of: 

- Accentuating and extracting features of the data
- Labelling of high-level calsses based on relationship scores ascertained in data gathering
- Fitting models to these labelled classes with machine learning algorithms
- Testing the predictive power of these models on unlabelled data

**Extended Application**

The developer branch of this repository will be used to implement additional features not required for the study for which the app was originally designed. This will include implementation of additional sensors with compatibility checks, real-time visualisation of sensor data, and user control over which sensors are activated.
