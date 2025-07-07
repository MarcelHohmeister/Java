public class Auto {
    private String _name;
    private int _velocity;
    private float _disturbanceFactor;

    public Auto(String carName, int velocity, float disturbanceFactor){
        _name = carName;
        _velocity = velocity;
        _disturbanceFactor = disturbanceFactor;
    }

    public void accelerate(int _acceleration){
        _velocity += _acceleration;
    }

    public void brake(int newVelocity){
        _velocity = newVelocity;
    }

    public void setSpeed(int newSpeed){
        _velocity = newSpeed;
    }

    public String getName(){
        return _name;
    }

    public int getSpeed(){
        return _velocity;
    }

    public float getTimeWasteFactor(){
        return _disturbanceFactor;
    }
}
