package es.ua.sd.central.service;

import es.ua.sd.central.db.WaterStationDao;
import es.ua.sd.central.model.StationStatus;
import es.ua.sd.central.model.WaterStation;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static es.ua.sd.central.model.StationStatus.AVERIA;
import static es.ua.sd.central.model.StationStatus.DESCONECTADA;
import static es.ua.sd.central.model.StationStatus.FUERA_DE_SERVICIO;
import static es.ua.sd.central.model.StationStatus.REGANDO;
import static es.ua.sd.central.model.StationStatus.REPOSO;


public class StationService {

    private static final Map<StationStatus, Set<StationStatus>> ALLOWED = Map.of(
            DESCONECTADA,      Set.of(),
            REPOSO,            Set.of(REGANDO, AVERIA, FUERA_DE_SERVICIO),
            REGANDO,           Set.of(REPOSO, AVERIA, FUERA_DE_SERVICIO),
            AVERIA,            Set.of(REPOSO),
            FUERA_DE_SERVICIO, Set.of(REPOSO)
    );

    private final WaterStationDao stationDao;

    public StationService(WaterStationDao stationDao) {
        this.stationDao = stationDao;
    }

    /** Punto 1 de la mecánica: al arrancar CENTRAL no conoce el estado real. */
    public synchronized void resetAllToDisconnected() throws SQLException {
        stationDao.updateAllStatus(DESCONECTADA);
    }

    /**
     * Alta de una estación nueva (Id + ubicación).
     * @return false si ya existía una estación con ese id.
     */
    public synchronized boolean register(String id, String location) throws SQLException {
        if (stationDao.findById(id).isPresent()) {
            return false;
        }
        stationDao.insert(new WaterStation(id, location, DESCONECTADA));
        return true;
    }

    /**
     * Autenticación del Monitor (WM_WS_M) al arrancar.
     * @return false si el id no está dado de alta (se rechaza la conexión).
     */
    public synchronized boolean authenticate(String id) throws SQLException {
        if (stationDao.findById(id).isEmpty()) {
            return false;
        }
        stationDao.updateStatus(id, REPOSO);
        return true;
    }

    /** Se llama cuando se cae el socket del Monitor (ClientHandler, bloque finally). */
    public synchronized void markDisconnected(String id) throws SQLException {
        if (stationDao.findById(id).isPresent()) {
            stationDao.updateStatus(id, DESCONECTADA);
        }
    }


    public synchronized boolean changeStatus(String id, StationStatus newStatus) throws SQLException {
        Optional<WaterStation> station = stationDao.findById(id);
        if (station.isEmpty()) {
            return false;
        }
        StationStatus current = station.get().status();
        if (!ALLOWED.get(current).contains(newStatus)) {
            return false;
        }
        stationDao.updateStatus(id, newStatus);
        return true;
    }

    public synchronized Optional<WaterStation> findById(String id) throws SQLException {
        return stationDao.findById(id);
    }

    public synchronized List<WaterStation> findAll() throws SQLException {
        return stationDao.findAll();
    }
}
