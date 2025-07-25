package com.automak_sensors.automak_device_management.api.controller;

import com.automak_sensors.automak_device_management.api.client.SensorMonitoringClient;
import com.automak_sensors.automak_device_management.api.model.SensorInput;
import com.automak_sensors.automak_device_management.api.model.SensorOutput;
import com.automak_sensors.automak_device_management.common.IdGenerator;
import com.automak_sensors.automak_device_management.domain.model.Sensor;
import com.automak_sensors.automak_device_management.domain.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SensorMonitoringClient sensorMonitoringClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput createSensor(@RequestBody SensorInput input) {
        Sensor sensor = Sensor.builder()
                .id(IdGenerator.generateId())
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(input.getEnabled())
                .build();

        Sensor saved = sensorRepository.saveAndFlush(sensor);
        return SensorOutput.builder()
                .id(saved.getId())
                .name(saved.getName())
                .ip(saved.getIp())
                .location(saved.getLocation())
                .protocol(saved.getProtocol())
                .model(saved.getModel())
                .enabled(saved.getEnabled())
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorOutput> getSensorById(@PathVariable String id) {
        return sensorRepository.findById(id)
                .map(sensor -> ResponseEntity.ok(SensorOutput.builder()
                        .id(sensor.getId())
                        .name(sensor.getName())
                        .ip(sensor.getIp())
                        .location(sensor.getLocation())
                        .protocol(sensor.getProtocol())
                        .model(sensor.getModel())
                        .enabled(sensor.getEnabled())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<SensorOutput> getSensors(@PageableDefault Pageable pageable) {
        return sensorRepository.findAll(pageable).map(sensor -> SensorOutput.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .location(sensor.getLocation())
                .protocol(sensor.getProtocol())
                .model(sensor.getModel())
                .enabled(sensor.getEnabled())
                .build());
    }

    @PutMapping("/{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableSensor(@PathVariable String sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(true);
        sensorRepository.save(sensor);
        sensorMonitoringClient.enableMonitoring(sensorId);
    }

    @DeleteMapping("/{sensorId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableSensor(@PathVariable String sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(false);
        sensorRepository.save(sensor);
        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    private SensorOutput convertToModel(Sensor sensor) {
        return SensorOutput.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .location(sensor.getLocation())
                .protocol(sensor.getProtocol())
                .model(sensor.getModel())
                .enabled(sensor.getEnabled())
                .build();
    }

}
