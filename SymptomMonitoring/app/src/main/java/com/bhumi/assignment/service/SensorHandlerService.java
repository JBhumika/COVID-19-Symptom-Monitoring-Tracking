package com.bhumi.assignment.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import com.bhumi.assignment.constants.SymptomsConstants;
import java.util.ArrayList;
import java.util.List;

public class SensorHandlerService extends Service implements SensorEventListener {

  private SensorManager accelerometerManage;
  float[] accelerometerValuesX = new float[SymptomsConstants.INTEGER_450];
  float[] accelerometerValuesY = new float[SymptomsConstants.INTEGER_450];
  float[] accelerometerValuesZ = new float[SymptomsConstants.INTEGER_450];
  int index = SymptomsConstants.ZERO;

  public SensorHandlerService() {
  }

  @Override
  public void onCreate() {

    accelerometerManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    final Sensor senseAccelerometer = accelerometerManage.getDefaultSensor(
        Sensor.TYPE_ACCELEROMETER);
    accelerometerManage.registerListener(this, senseAccelerometer,
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {

    Sensor mySensor = sensorEvent.sensor;

    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      index++;
      accelerometerValuesX[index] = sensorEvent.values[SymptomsConstants.ZERO];
      accelerometerValuesY[index] = sensorEvent.values[SymptomsConstants.ONE];
      accelerometerValuesZ[index] = sensorEvent.values[SymptomsConstants.TWO];
      if (index >= SymptomsConstants.INTEGER_449) {
        index = SymptomsConstants.ZERO;
        accelerometerManage.unregisterListener(this);
        callMeasureRespRate();
      }
    }
  }

  //calculating the respiratory rate
  private void callMeasureRespRate() {

    for (int i = SymptomsConstants.ZERO, j = SymptomsConstants.INTEGER_20;
         j < SymptomsConstants.INTEGER_450; i++, j++) {
      float sum = SymptomsConstants.ZERO;
      for (int k = i; k < j; k++) {
        sum += accelerometerValuesX[k];
      }
      accelerometerValuesY[i] = sum / SymptomsConstants.INTEGER_20;
    }

    List<Integer> ext = new ArrayList<>();
    for (int i = SymptomsConstants.ZERO;
         i < accelerometerValuesY.length - SymptomsConstants.INTEGER_20; i++) {
      if ((accelerometerValuesY[i + SymptomsConstants.ONE] - accelerometerValuesY[i]) * (
          accelerometerValuesY[i + SymptomsConstants.TWO] - accelerometerValuesY[i
              + SymptomsConstants.ONE])
          <= SymptomsConstants.ZERO) {
        ext.add(i + SymptomsConstants.ONE);
      }
    }

    int respiratoryRate = SymptomsConstants.ZERO;
    for (int i = SymptomsConstants.ZERO; i < ext.size() - SymptomsConstants.ONE; i++) {
      if (ext.get(i) / SymptomsConstants.TEN != ext.get(i++)) respiratoryRate++;
    }
    respiratoryRate /= SymptomsConstants.TWO;

    Intent intent = new Intent();
    intent.setAction(SymptomsConstants.CALC_RESP_RATE);
    intent.putExtra("RESP_RATE_RETURNED", respiratoryRate);
    sendBroadcast(intent);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
  }
}
