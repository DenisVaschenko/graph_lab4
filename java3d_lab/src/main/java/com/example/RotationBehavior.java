package com.example;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedTime;

public class RotationBehavior extends Behavior {
    
    private TransformGroup target;
    private Transform3D rotation;
    private WakeupCondition condition;
    int count;
    public RotationBehavior(TransformGroup target){
        this.target = target;
    }
    @Override
    public void initialize() {
        condition = new WakeupOnElapsedTime(25);
        wakeupOn(condition);
    }

    @Override
    public void processStimulus(Enumeration criteria) {
        if (count >= 100 || rotation == null){
            wakeupOn(condition);
            return;
        }
        Transform3D t3d = new Transform3D();
        target.getTransform(t3d);
        Transform3D newTransform = new Transform3D();
        newTransform.mul(rotation, t3d);
        // Оновлюємо трансформацію
        target.setTransform(newTransform);
        // Перезапускаємо умови для наступного кадру
        count++;
        wakeupOn(condition);
    }
    public void rotate(int axis, float angle){
        rotation = new Transform3D();
        if (axis == 0) rotation.rotX(angle / 100);
        else if (axis == 1) rotation.rotY(angle / 100);
        else rotation.rotZ(angle / 100);// Оновлення кожні 50 мс
        count = 0;
    }
}
