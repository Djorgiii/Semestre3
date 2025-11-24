package agentesautonomos;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Boid extends Body {

    private float maximumSpeed;
    private float maximumForce;

    public Boid(PApplet app, PVector position, float mass, float radius, int color) {
        super(app, position, new PVector(0,0), mass, radius, color);
        
        this.maximumSpeed = 200f;
        this.maximumForce = 500f;
    }

    public void arrive(PVector targetPosition, float slowingRadius) {
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        float distanceToTarget = desiredVelocity.mag();
        
        if (distanceToTarget < 5.0f) { 
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
    

    public void seek(PVector targetPosition) {
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        
        desiredVelocity.normalize();
        desiredVelocity.mult(maximumSpeed);

        PVector steeringForce = PVector.sub(desiredVelocity, velocity);
        
        steeringForce.limit(maximumForce);

        applyForce(steeringForce);
    }

    @Override
    public void move(float secondsElapsed) {
        super.move(secondsElapsed);
        velocity.limit(maximumSpeed);
    }

    @Override
    public void display() {
        app.pushMatrix();
        app.translate(position.x, position.y);
        
        float rotationAngle = velocity.heading(); 
        app.rotate(rotationAngle);

        app.pushStyle();
        app.fill(color);
        app.noStroke();
        
        app.beginShape();
        app.vertex(radius * 2, 0);
        app.vertex(-radius, -radius);
        app.vertex(-radius, radius);
        app.endShape(PConstants.CLOSE);
        
        app.popStyle();
        
        app.popMatrix();
    }


    public void setMaximumSpeed(float speed) {
        this.maximumSpeed = speed;
        if (this.maximumSpeed < 0) this.maximumSpeed = 0; 
    }
    
    public void setMaximumForce(float force) {
        this.maximumForce = force;
        if (this.maximumForce < 0) this.maximumForce = 0;
    }
    
    public float getMaximumSpeed() { return maximumSpeed; }
    public float getMaximumForce() { return maximumForce; }
    
    public void brake() {
        
        if (velocity.mag() < 10.0f) {
            velocity.mult(0);
            return;
        }
        
        PVector brakingForce = velocity.copy();
        brakingForce.mult(-1);
        brakingForce.normalize();
        brakingForce.mult(maximumForce * 0.5f);
        applyForce(brakingForce);
    }
}