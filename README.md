# Bluetooth Low Energy (BLE) communication with Android 

This is a sample project to demonstrate the communication between BLE and Android. Most of the tutorial is confusing. However, my BLE device is a notification type device which will send notifications if there are any changes in the device. I set up this project in order to test a BLE device which was sent from a hardware company. Anyway, i will add a video to show demo.

You need to add theses permission in `AndroidManifest.xml` 


    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<a href="https://www.youtube.com/watch?v=sIGmHR6_PVM" target="_blank">DEMO</a>
