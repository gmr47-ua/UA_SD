package es.ua.sd.central.model;
import java.time.Instant;
public record Irrigation(int id, String stationId, Integer operatorId,
                         Instant startedAt, Instant endedAt,
                         Double volume, String result) {}
