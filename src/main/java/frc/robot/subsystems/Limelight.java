package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Limelight extends SubsystemBase{
    // limelight network table
    public NetworkTable limelightTable = null;

    // constants
    public final double CAMERA_HEIGHT = 240; // pixels
    public final double CAMERA_FOV = Math.toRadians(49.7); // rads
    private final double FEET_TO_METERS = 0.3048;
    private final double RAD_TO_DEGREES = 180 / Math.PI;
    private final double formulaRatio = 55.029;
    private final double portHeight = 8.1875; // meters
    private final double tapeHeight = 2.5 / 2; // the height between the bottom and top of the tape in meters
    private final double shooterHeight = 43 / 12; // meters
    private final double portDist = 29.25 / 12; // the horizontal distance between the inner and outer ports in meters
    private final double heightDiff = portHeight - shooterHeight; // difference in height in meters
    private final double camAng = 45; //degrees
    private double vel, dist, angDist;

    // LED mode enum
    public enum LedMode {
        DEFAULT(0), OFF(1), BLINK(2), ON(3);

        private final int value;
        LedMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }

    // camera mode enum
    public enum CameraMode {
        VISION(0), CAMERA(1);

        private final int value;
        CameraMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return this.value;
        }
    }

    /**
     * Constructor for Limelight.
     */
    public Limelight() {
        limelightTable = NetworkTableInstance.getDefault().getTable("limelight");
        SmartDashboard.putNumber("RPM Adjuster", 0.0);
    }

    @Override
    public void periodic(){
        updateShuffleboard();
        setDist();
    }

    public void updateShuffleboard(){
        vel = SmartDashboard.getNumber("RPM Adjuster", 0.0);
        SmartDashboard.putNumber("Formula RPM", formulaRPM());
    }

    /**
     * Sets the LED mode of the camera.
     * @param mode the LED mode to set the camera to
     */
    public void setLightMode(LedMode mode) {
        limelightTable.getEntry("ledMode").setNumber(mode.getValue());
    }

    /**
     * Sets the camera mode of the camera.
     * @param mode the camera mode to set the camera to
     */
    public void setCameraMode(CameraMode mode) {
        limelightTable.getEntry("camMode").setNumber(mode.getValue());
    }

    /**
     * Returns if the camera sees a target.
     */
    public boolean hasTarget() {
        return limelightTable.getEntry("tv").getBoolean(false);
    }

    /**
     * Returns the vertical angle from the center of the camera to the target.
     */
    public double getVerticalAngle() {
        return limelightTable.getEntry("ty").getDouble(0.0);
    }

    /**
     * Returns the horizontal angle from the center of the camera to the target.
     */
    public double getHorizontalAngle() {
        return limelightTable.getEntry("tx").getDouble(0.0);
    }

    /** Returns distance in meters from object of height s (feet). 
     *  Uses s = r(theta). */
    public double getDistance(double objectHeight) {
        double boxHeight = limelightTable.getEntry("tvert").getDouble(0.0); // pixels
        if(boxHeight == 0) return 0;
        double percentHeight = boxHeight / CAMERA_HEIGHT;
        double boxDegree = percentHeight * CAMERA_FOV;
        double r = objectHeight / boxDegree;
        return r; // from front of bot
    }

    public double formulaRPM(){
        return (formulaRatio * angDist) + vel;
    }

    /**
     * Start tracking the vision targets
     */
    public void trackTarget() {
        setLightMode(LedMode.ON);
        setCameraMode(CameraMode.VISION);
    }

    /**
     * Use LimeLight as camera
     */
    public void useAsCamera() {
        setLightMode(LedMode.OFF);
        setCameraMode(CameraMode.CAMERA);
    }

    public void setDist(){
        dist = getDistance(tapeHeight);
        angDist = dist * Math.tan((getVerticalAngle() + camAng) / RAD_TO_DEGREES);
    }
}