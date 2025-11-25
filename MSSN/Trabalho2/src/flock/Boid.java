package flock;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import java.util.ArrayList;

public class Boid extends Body {

    private float maximumSpeed;
    private float maximumForce;
    private float lifeTimer;

    public Boid(PApplet app, PVector position, float mass, float radius, int color) {
        super(app, position, new PVector(0,0), mass, radius, color);
        
        this.maximumSpeed = 300.0f; 
        this.maximumForce = 500.0f; 
        this.lifeTimer = app.random(2, 5);
    }

    public PVector calculateSeparationForce(ArrayList<Boid> nearbyBoids) {
        float desiredSeparation = 25.0f;
        PVector sumOfRepulsions = new PVector(0, 0, 0);
        int neighborCount = 0;

        for (Boid otherBoid : nearbyBoids) {
            float distanceToNeighbor = PVector.dist(position, otherBoid.position);
            
            if ((distanceToNeighbor > 0) && (distanceToNeighbor < desiredSeparation)) {
                
                PVector vectorAway = PVector.sub(position, otherBoid.position);
                vectorAway.normalize();
                vectorAway.div(distanceToNeighbor);
                
                sumOfRepulsions.add(vectorAway);
                neighborCount++;
            }
        }

        if (neighborCount > 0) {
            sumOfRepulsions.div((float)neighborCount);
        }

        if (sumOfRepulsions.mag() > 0) {
            sumOfRepulsions.setMag(maximumSpeed);
            sumOfRepulsions.sub(velocity);
            sumOfRepulsions.limit(maximumForce);
        }
        return sumOfRepulsions;
    }

    public void applyArriveBehavior(PVector targetPosition, float slowingRadius, boolean shouldSnapToTarget) {
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        float distanceToTarget = desiredVelocity.mag();
        
        if (shouldSnapToTarget && distanceToTarget < 15.0f) { 
            velocity.mult(0); 
            this.position = targetPosition.copy(); 
            return;
        }
        
        PVector steeringForce;
        
        if (distanceToTarget < slowingRadius) {
            double brakingCurveExponent = 1.0;
            
            float rampingFactor = (float) Math.pow(distanceToTarget / slowingRadius, brakingCurveExponent);
            float desiredSpeed = maximumSpeed * rampingFactor;
            
            desiredVelocity.setMag(desiredSpeed);
            
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce * 10.0f);
            
            applyForce(steeringForce);
            
            if (velocity.mag() > desiredSpeed) {
                velocity.setMag(desiredSpeed);
            }

        } else {
            desiredVelocity.setMag(maximumSpeed);
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce); 
            applyForce(steeringForce);
        }
    }


    public void decreaseLifeTimer(float secondsElapsed) {
        this.lifeTimer -= secondsElapsed;
    }

    public boolean isDead() {
        return this.lifeTimer < 0;
    }

    public void setLifeTimer(float seconds) {
        this.lifeTimer = seconds;
    }
    
    public void setVelocityManually(PVector newVelocity) {
        this.velocity = newVelocity.copy();
    }

    public void setMaximumSpeed(float speed) { this.maximumSpeed = speed; }
    public void setMaximumForce(float force) { this.maximumForce = force; }
    public float getMaximumSpeed() { return maximumSpeed; }
    public float getMaximumForce() { return maximumForce; }

    @Override
    public void display() {
        if (lifeTimer < 1.0f && (app.millis() / 100) % 2 == 0) {
            app.fill(255, 0, 0); 
        } else {
            app.fill(color);
        }
        
        app.pushMatrix();
        app.translate(position.x, position.y);
        
        float rotationAngle = velocity.heading();
        app.rotate(rotationAngle);
        
        app.noStroke();
        app.beginShape();
        app.vertex(radius * 2, 0);
        app.vertex(-radius, -radius);
        app.vertex(-radius, radius);
        app.endShape(PConstants.CLOSE);
        
        app.popMatrix();
    }
}