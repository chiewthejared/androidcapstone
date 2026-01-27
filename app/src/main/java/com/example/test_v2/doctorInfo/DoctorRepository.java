package com.example.test_v2.doctorInfo;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorRepository {

    private final DoctorDao doctorDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DoctorRepository(Application application) {
        DoctorDatabase db = DoctorDatabase.getDatabase(application);
        doctorDao = db.doctorDao();
    }

    public LiveData<List<DoctorItem>> getDoctors(String userId) {
        return doctorDao.getDoctorsByUser(userId);
    }

    public void insert(DoctorItem doctor) {
        executorService.execute(() -> doctorDao.insert(doctor));
    }

    public void update(DoctorItem doctor) {
        executorService.execute(() -> doctorDao.update(doctor));
    }

    public void delete(DoctorItem doctor) {
        executorService.execute(() -> doctorDao.delete(doctor));
    }
}