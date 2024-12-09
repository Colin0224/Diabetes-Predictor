package com.example.diabetes_predictor;

import java.lang.reflect.Field;

public class DiabetesRecord {
    private double diabetesBinary;
    private double highBP;
    private double highChol;
    private double cholCheck;
    private double bmi;
    private double smoker;
    private double stroke;
    private double heartDiseaseOrAttack;
    private double physActivity;
    private double fruits;
    private double veggies;
    private double hvyAlcoholConsump;
    private double anyHealthcare;
    private double noDocbcCost;
    private double genHlth;
    private double mentHlth;
    private double physHlth;
    private double diffWalk;
    private double sex;
    private double age;
    private double education;
    private double income;

    public double getDiabetesBinary() { return diabetesBinary; }
    public void setDiabetesBinary(double diabetesBinary) { this.diabetesBinary = diabetesBinary; }

    public double getHighBP() { return highBP; }
    public void setHighBP(double highBP) { this.highBP = highBP; }

    public double getHighChol() { return highChol; }
    public void setHighChol(double highChol) { this.highChol = highChol; }

    public double getCholCheck() { return cholCheck; }
    public void setCholCheck(double cholCheck) { this.cholCheck = cholCheck; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public double getSmoker() { return smoker; }
    public void setSmoker(double smoker) { this.smoker = smoker; }

    public double getStroke() { return stroke; }
    public void setStroke(double stroke) { this.stroke = stroke; }

    public double getHeartDiseaseOrAttack() { return heartDiseaseOrAttack; }
    public void setHeartDiseaseOrAttack(double heartDiseaseOrAttack) { this.heartDiseaseOrAttack = heartDiseaseOrAttack; }

    public double getPhysActivity() { return physActivity; }
    public void setPhysActivity(double physActivity) { this.physActivity = physActivity; }

    public double getFruits() { return fruits; }
    public void setFruits(double fruits) { this.fruits = fruits; }

    public double getVeggies() { return veggies; }
    public void setVeggies(double veggies) { this.veggies = veggies; }

    public double getHvyAlcoholConsump() { return hvyAlcoholConsump; }
    public void setHvyAlcoholConsump(double hvyAlcoholConsump) { this.hvyAlcoholConsump = hvyAlcoholConsump; }

    public double getAnyHealthcare() { return anyHealthcare; }
    public void setAnyHealthcare(double anyHealthcare) { this.anyHealthcare = anyHealthcare; }

    public double getNoDocbcCost() { return noDocbcCost; }
    public void setNoDocbcCost(double noDocbcCost) { this.noDocbcCost = noDocbcCost; }

    public double getGenHlth() { return genHlth; }
    public void setGenHlth(double genHlth) { this.genHlth = genHlth; }

    public double getMentHlth() { return mentHlth; }
    public void setMentHlth(double mentHlth) { this.mentHlth = mentHlth; }

    public double getPhysHlth() { return physHlth; }
    public void setPhysHlth(double physHlth) { this.physHlth = physHlth; }

    public double getDiffWalk() { return diffWalk; }
    public void setDiffWalk(double diffWalk) { this.diffWalk = diffWalk; }

    public double getSex() { return sex; }
    public void setSex(double sex) { this.sex = sex; }

    public double getAge() { return age; }
    public void setAge(double age) { this.age = age; }

    public double getEducation() { return education; }
    public void setEducation(double education) { this.education = education; }

    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public Object getFieldValue(String fieldName){
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException e) {
            System.out.println("Error: Field '" + fieldName + "' not found.");
        } catch (IllegalAccessException e) {
            System.out.println("Error: Unable to access field '" + fieldName + "'.");
        }
        return null;
    }

    @Override
    public String toString() {
        return "DiabetesRecord{" +
                "diabetesBinary=" + diabetesBinary +
                ", highBP=" + highBP +
                ", highChol=" + highChol +
                ", cholCheck=" + cholCheck +
                ", bmi=" + bmi +
                ", smoker=" + smoker +
                ", stroke=" + stroke +
                ", heartDiseaseOrAttack=" + heartDiseaseOrAttack +
                ", physActivity=" + physActivity +
                ", fruits=" + fruits +
                ", veggies=" + veggies +
                ", hvyAlcoholConsump=" + hvyAlcoholConsump +
                ", anyHealthcare=" + anyHealthcare +
                ", noDocbcCost=" + noDocbcCost +
                ", genHlth=" + genHlth +
                ", mentHlth=" + mentHlth +
                ", physHlth=" + physHlth +
                ", diffWalk=" + diffWalk +
                ", sex=" + sex +
                ", age=" + age +
                ", education=" + education +
                ", income=" + income +
                '}';
    }
}
