package com.example.test_v2.doctorInfo;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class DoctorViewModel extends AndroidViewModel {

    private final DoctorRepository repository;
    private LiveData<List<DoctorItem>> allDoctors;

    public DoctorViewModel(@NonNull Application application) {
        super(application);
        repository = new DoctorRepository(application);

        String userId = application.getSharedPreferences("UserSession", Application.MODE_PRIVATE)
                .getString("loggedInPin", null);

        if (userId != null) {
            allDoctors = repository.getDoctors(userId);
        }
    }

    public LiveData<List<DoctorItem>> getAllDoctors() {
        return allDoctors;
    }

    public void insert(DoctorItem doctor) {
        repository.insert(doctor);
    }

    public void update(DoctorItem doctor) {
        repository.update(doctor);
    }

    public void delete(DoctorItem doctor) {
        repository.delete(doctor);
    }
}