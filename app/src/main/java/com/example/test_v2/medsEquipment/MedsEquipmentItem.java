package com.example.test_v2.medsEquipment;

public class MedsEquipmentItem {
    private String id;
    private String name;
    private String time;
    private String dosage;
    private String date;
    private String description;
    private String otherNames;
    private String specialInstructions;
    private String bottleDescription;
    private String sideEffects;
    private String prescribingDoctor;
    private String reasonPrescribed;
    private String notes;
    private String reminder2Weeks;
    private String reminder1Week;
    private String reminder5Days;
    private String reminder3Days;
    private String reminder1Day;
    private String reminderDayOf;
    private String tag;

    // Equipment-specific fields
    private String serialNumber;
    private String weight;
    private String size;
    private String datePrescribed;
    private String insuranceUsed;
    private String datePurchased;
    private String goodThrough;
    private String replacementAvailableOn;
    private String equipmentProvider;
    private String loanedOrPurchased;
    private String maintenanceProvider;
    private String lastMaintenance;
    private String sparePartsTools;
    private String associatedComponents;
    private String equipmentNotes;

    // Supply-specific fields
    private String preferredBrand;
    private String alternativeBrands;
    private String sku;
    private String orderQuantity;
    private String orderFrequency;
    private String refillsRemaining;
    private String supplyCompany;
    private String supplyCompanyPhone;
    private String supplyCompanyAddress;
    private String lastOrderDate;
    private String expectedDeliveryDate;
    private String expiryDate;
    private String alternateSources;
    private String supplyNotes;

    // ======== Constructors ========

    // Medication constructor
    public MedsEquipmentItem(String id, String name, String dosage, String time, String date, String description,
                             String otherNames, String specialInstructions, String bottleDescription,
                             String sideEffects, String prescribingDoctor, String reasonPrescribed, String notes,
                             String reminder2Weeks, String reminder1Week, String reminder5Days, String reminder3Days,
                             String reminder1Day, String reminderDayOf, String tag) {
        this.id = id;
        this.name = name;
        this.dosage = (dosage == null || dosage.isEmpty()) ? "N/A" : dosage;
        this.time = time;
        this.date = date;
        this.description = description;
        this.otherNames = otherNames;
        this.specialInstructions = specialInstructions;
        this.bottleDescription = bottleDescription;
        this.sideEffects = sideEffects;
        this.prescribingDoctor = prescribingDoctor;
        this.reasonPrescribed = reasonPrescribed;
        this.notes = notes;
        this.reminder2Weeks = reminder2Weeks;
        this.reminder1Week = reminder1Week;
        this.reminder5Days = reminder5Days;
        this.reminder3Days = reminder3Days;
        this.reminder1Day = reminder1Day;
        this.reminderDayOf = reminderDayOf;
        this.tag = tag;
    }

    // Equipment constructor
    public MedsEquipmentItem(String id, String name, String date, String description,
                             String serialNumber, String weight, String size, String prescribingDoctor,
                             String datePrescribed, String insuranceUsed, String datePurchased, String goodThrough,
                             String replacementAvailableOn, String equipmentProvider, String loanedOrPurchased,
                             String maintenanceProvider, String lastMaintenance, String sparePartsTools,
                             String associatedComponents, String equipmentNotes,
                             String reminder2Weeks, String reminder1Week, String reminder5Days,
                             String reminder3Days, String reminder1Day, String reminderDayOf, String tag) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.serialNumber = serialNumber;
        this.weight = weight;
        this.size = size;
        this.prescribingDoctor = prescribingDoctor;
        this.datePrescribed = datePrescribed;
        this.insuranceUsed = insuranceUsed;
        this.datePurchased = datePurchased;
        this.goodThrough = goodThrough;
        this.replacementAvailableOn = replacementAvailableOn;
        this.equipmentProvider = equipmentProvider;
        this.loanedOrPurchased = loanedOrPurchased;
        this.maintenanceProvider = maintenanceProvider;
        this.lastMaintenance = lastMaintenance;
        this.sparePartsTools = sparePartsTools;
        this.associatedComponents = associatedComponents;
        this.equipmentNotes = equipmentNotes;
        this.reminder2Weeks = reminder2Weeks;
        this.reminder1Week = reminder1Week;
        this.reminder5Days = reminder5Days;
        this.reminder3Days = reminder3Days;
        this.reminder1Day = reminder1Day;
        this.reminderDayOf = reminderDayOf;
        this.tag = tag;

        // Med-specific defaults
        this.dosage = "N/A";
        this.time = "";
        this.otherNames = "";
        this.specialInstructions = "";
        this.bottleDescription = "";
        this.sideEffects = "";
        this.reasonPrescribed = "";
        this.notes = "";
    }

    // Supply constructor
    public MedsEquipmentItem(String id, String name, String date, String description,
                             String preferredBrand, String alternativeBrands, String sku, String size,
                             String orderQuantity, String orderFrequency, String refillsRemaining,
                             String supplyCompany, String supplyCompanyPhone, String supplyCompanyAddress,
                             String prescribingDoctor, String datePrescribed, String insuranceUsed,
                             String lastOrderDate, String expectedDeliveryDate, String expiryDate,
                             String alternateSources, String supplyNotes,
                             String reminder2Weeks, String reminder1Week, String reminder5Days,
                             String reminder3Days, String reminder1Day, String reminderDayOf, String tag) {
        this.id = id;
        this.name = name;
        this.date = date; // Next Order Date
        this.description = description;
        this.preferredBrand = preferredBrand;
        this.alternativeBrands = alternativeBrands;
        this.sku = sku;
        this.size = size;
        this.orderQuantity = orderQuantity;
        this.orderFrequency = orderFrequency;
        this.refillsRemaining = refillsRemaining;
        this.supplyCompany = supplyCompany;
        this.supplyCompanyPhone = supplyCompanyPhone;
        this.supplyCompanyAddress = supplyCompanyAddress;
        this.prescribingDoctor = prescribingDoctor;
        this.datePrescribed = datePrescribed;
        this.insuranceUsed = insuranceUsed;
        this.lastOrderDate = lastOrderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.expiryDate = expiryDate;
        this.alternateSources = alternateSources;
        this.supplyNotes = supplyNotes;
        this.reminder2Weeks = reminder2Weeks;
        this.reminder1Week = reminder1Week;
        this.reminder5Days = reminder5Days;
        this.reminder3Days = reminder3Days;
        this.reminder1Day = reminder1Day;
        this.reminderDayOf = reminderDayOf;
        this.tag = tag;

        // Default med/equip fields to empty
        this.dosage = "N/A";
        this.time = "";
        this.serialNumber = "";
        this.weight = "";
        this.equipmentProvider = "";
        this.loanedOrPurchased = "";
        this.maintenanceProvider = "";
        this.lastMaintenance = "";
        this.sparePartsTools = "";
        this.associatedComponents = "";
        this.equipmentNotes = "";
        this.otherNames = "";
        this.specialInstructions = "";
        this.bottleDescription = "";
        this.sideEffects = "";
        this.reasonPrescribed = "";
        this.notes = "";
    }

    // ================= Getters =================
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getOtherNames() { return otherNames; }
    public String getSpecialInstructions() { return specialInstructions; }
    public String getBottleDescription() { return bottleDescription; }
    public String getSideEffects() { return sideEffects; }
    public String getPrescribingDoctor() { return prescribingDoctor; }
    public String getReasonPrescribed() { return reasonPrescribed; }
    public String getNotes() { return notes; }
    public String getReminder2Weeks() { return reminder2Weeks; }
    public String getReminder1Week() { return reminder1Week; }
    public String getReminder5Days() { return reminder5Days; }
    public String getReminder3Days() { return reminder3Days; }
    public String getReminder1Day() { return reminder1Day; }
    public String getReminderDayOf() { return reminderDayOf; }
    public String getTag() { return tag; }

    // Equipment getters
    public String getSerialNumber() { return serialNumber; }
    public String getWeight() { return weight; }
    public String getSize() { return size; }
    public String getDatePrescribed() { return datePrescribed; }
    public String getInsuranceUsed() { return insuranceUsed; }
    public String getDatePurchased() { return datePurchased; }
    public String getGoodThrough() { return goodThrough; }
    public String getReplacementAvailableOn() { return replacementAvailableOn; }
    public String getEquipmentProvider() { return equipmentProvider; }
    public String getLoanedOrPurchased() { return loanedOrPurchased; }
    public String getMaintenanceProvider() { return maintenanceProvider; }
    public String getLastMaintenance() { return lastMaintenance; }
    public String getSparePartsTools() { return sparePartsTools; }
    public String getAssociatedComponents() { return associatedComponents; }
    public String getEquipmentNotes() { return equipmentNotes; }

    // Supply getters
    public String getPreferredBrand() { return preferredBrand; }
    public String getAlternativeBrands() { return alternativeBrands; }
    public String getSku() { return sku; }
    public String getOrderQuantity() { return orderQuantity; }
    public String getOrderFrequency() { return orderFrequency; }
    public String getRefillsRemaining() { return refillsRemaining; }
    public String getSupplyCompany() { return supplyCompany; }
    public String getSupplyCompanyPhone() { return supplyCompanyPhone; }
    public String getSupplyCompanyAddress() { return supplyCompanyAddress; }
    public String getLastOrderDate() { return lastOrderDate; }
    public String getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getAlternateSources() { return alternateSources; }
    public String getSupplyNotes() { return supplyNotes; }

    // ================= Setters =================
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setOtherNames(String otherNames) { this.otherNames = otherNames; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public void setBottleDescription(String bottleDescription) { this.bottleDescription = bottleDescription; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
    public void setPrescribingDoctor(String prescribingDoctor) { this.prescribingDoctor = prescribingDoctor; }
    public void setReasonPrescribed(String reasonPrescribed) { this.reasonPrescribed = reasonPrescribed; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setReminder2Weeks(String reminder2Weeks) { this.reminder2Weeks = reminder2Weeks; }
    public void setReminder1Week(String reminder1Week) { this.reminder1Week = reminder1Week; }
    public void setReminder5Days(String reminder5Days) { this.reminder5Days = reminder5Days; }
    public void setReminder3Days(String reminder3Days) { this.reminder3Days = reminder3Days; }
    public void setReminder1Day(String reminder1Day) { this.reminder1Day = reminder1Day; }
    public void setReminderDayOf(String reminderDayOf) { this.reminderDayOf = reminderDayOf; }
    public void setTag(String tag) { this.tag = tag; }

    // Equipment setters
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setSize(String size) { this.size = size; }
    public void setDatePrescribed(String datePrescribed) { this.datePrescribed = datePrescribed; }
    public void setInsuranceUsed(String insuranceUsed) { this.insuranceUsed = insuranceUsed; }
    public void setDatePurchased(String datePurchased) { this.datePurchased = datePurchased; }
    public void setGoodThrough(String goodThrough) { this.goodThrough = goodThrough; }
    public void setReplacementAvailableOn(String replacementAvailableOn) { this.replacementAvailableOn = replacementAvailableOn; }
    public void setEquipmentProvider(String equipmentProvider) { this.equipmentProvider = equipmentProvider; }
    public void setLoanedOrPurchased(String loanedOrPurchased) { this.loanedOrPurchased = loanedOrPurchased; }
    public void setMaintenanceProvider(String maintenanceProvider) { this.maintenanceProvider = maintenanceProvider; }
    public void setLastMaintenance(String lastMaintenance) { this.lastMaintenance = lastMaintenance; }
    public void setSparePartsTools(String sparePartsTools) { this.sparePartsTools = sparePartsTools; }
    public void setAssociatedComponents(String associatedComponents) { this.associatedComponents = associatedComponents; }
    public void setEquipmentNotes(String equipmentNotes) { this.equipmentNotes = equipmentNotes; }

    // Supply setters
    public void setPreferredBrand(String preferredBrand) { this.preferredBrand = preferredBrand; }
    public void setAlternativeBrands(String alternativeBrands) { this.alternativeBrands = alternativeBrands; }
    public void setSku(String sku) { this.sku = sku; }
    public void setOrderQuantity(String orderQuantity) { this.orderQuantity = orderQuantity; }
    public void setOrderFrequency(String orderFrequency) { this.orderFrequency = orderFrequency; }
    public void setRefillsRemaining(String refillsRemaining) { this.refillsRemaining = refillsRemaining; }
    public void setSupplyCompany(String supplyCompany) { this.supplyCompany = supplyCompany; }
    public void setSupplyCompanyPhone(String supplyCompanyPhone) { this.supplyCompanyPhone = supplyCompanyPhone; }
    public void setSupplyCompanyAddress(String supplyCompanyAddress) { this.supplyCompanyAddress = supplyCompanyAddress; }
    public void setLastOrderDate(String lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    public void setExpectedDeliveryDate(String expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setAlternateSources(String alternateSources) { this.alternateSources = alternateSources; }
    public void setSupplyNotes(String supplyNotes) { this.supplyNotes = supplyNotes; }
}