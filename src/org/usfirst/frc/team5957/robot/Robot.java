package org.usfirst.frc.team5957.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	// Auto Names, driving modes, and driver controllers
	final String defaultAuto = "Do Nothing";
	final String middleAuto = "Middle Auto";
	final String leftAuto = "Left Auto";
	final String rightAuto = "Right Auto";
	final String demo = "Demo";
	final String blue = "Blue";
	final String red = "Red";
	final String defaultMode = "Sticks";
	final String gtaMode = "Triggers";
	final String flightStickMode = "Flight Stick";

	// PWM Values for motors and DI/O
	final int rearLeft = 0;
	final int frontLeft = 1;
	final int rearRight = 2;
	final int frontRight = 3;
	final int dump = 7;
	final int coil = 8;
	final int winch = 9;
	final int lowerSwitchChannel = 0;
	final int upperSwitchChannel = 1;

	// Controller values
	final int driverFlightXAxis = 1;
	final int driverFlightYAxis = 0;
	final int driverLeftXAxis = 0;
	final int driverLeftYAxis = 1;
	final int driverLeftTrigger = 2;
	final int driverRightTrigger = 3;
	final int driverRightXAxis = 4;
	final int driverRightYAxis = 5;
	final int operatorLeftXAxis = 0;
	final int operatorLeftYAxis = 1;
	final int operatorLeftTrigger = 2;
	final int operatorRightTrigger = 3;
	final int operatorRightXAxis = 4;
	final int operatorRightYAxis = 5;
	final int noRumble = 0;
	final int leftRumble = 1;
	final int rightRumble = 2;
	final int bothRumble = 3;
	final int lowGear = 1;
	final int highGear = 0;
	final int coilOut = 0;
	final int coilIn = 1;
	final int dumpUp = 0; // Angle on the DPad
	final int dumpDown = 180; // Angle on the DPad
	final int winchUp = 3;
	final int winchDown = 2;

	// Constants
	final double Kp = 0.03;
	final double highSpeed = 1.0;
	final double defaultSpeed = 0.5;
	final double lowSpeed = 0.2;
	final double turnSpeed = 0.5;
	final double coilSpeed = 1;
	final double dumpSpeed = 1;
	final double winchSpeed = 1;
	final double dropCoilTime = 4; // Time in seconds

	// Variables
	String position;
	String color;
	String drivingMode;
	String driverControl;

	// Motors or Subsystems
	RobotDrive oki;
	VictorSP dumpMotor;
	VictorSP coilMotor;
	VictorSP winchMotor;

	// SmartDashboard Choosers
	SendableChooser<String> positionChooser = new SendableChooser<>();
	SendableChooser<String> colorChooser = new SendableChooser<>();
	SendableChooser<String> drivingModeSwitch = new SendableChooser<>();

	// Inputs
	Joystick driver;
	Joystick operator;
	Timer timer;
	Gyro gyro;
	DigitalInput upperLimit;
	DigitalInput lowerLimit;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		positionChooser.addDefault("Do Nothing", defaultAuto);
		positionChooser.addObject("Drive Forward", middleAuto);
		positionChooser.addObject("Left Auto", leftAuto);
		positionChooser.addObject("Right Auto", rightAuto);
		positionChooser.addObject("Demo", demo);
		SmartDashboard.putData("Auto choices", positionChooser);

		drivingModeSwitch.addDefault("Sticks", defaultMode);
		drivingModeSwitch.addObject("Triggers", gtaMode);
		drivingModeSwitch.addObject("Flight Stick", flightStickMode);
		SmartDashboard.putData("Driving Modes", drivingModeSwitch);

		colorChooser.addDefault("Blue", blue);
		colorChooser.addObject("Red", red);
		SmartDashboard.putData("Team", colorChooser);

		oki = new RobotDrive(frontLeft, rearLeft, frontRight, rearRight);
		dumpMotor = new VictorSP(dump);
		coilMotor = new VictorSP(coil);
		winchMotor = new VictorSP(winch);
		driver = new Joystick(0);
		operator = new Joystick(1);
		gyro = new ADXRS450_Gyro();

		upperLimit = new DigitalInput(upperSwitchChannel);
		lowerLimit = new DigitalInput(lowerSwitchChannel);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		position = positionChooser.getSelected();
		drivingMode = drivingModeSwitch.getSelected();
		color = colorChooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);

		System.out.println("Team: " + color);
		System.out.println("Auto selected: " + position);
		System.out.println("Driving Mode: " + drivingMode);
	}

	/**
	 * This function is called periodically during autonomous All mentions of
	 * starting points are reference to the center of the back of the robot
	 * unless otherwise specified (Rule made by Ben)
	 */
	@Override
	public void autonomousPeriodic() {
		switch (position) {
		case middleAuto:
			// Planned starting point = pointing the coil at the middle spike
			moveForward(3);
			dropGear();
			moveBackward(2);
			if (color.equals(red)) {
				turnToAngleWithNeg(-60);
				moveForward(3);
				turnToAngle(60);
				moveForward(10);
			} else if (color.equalsIgnoreCase(blue)) {
				turnToAngle(60);
				moveForward(3);
				turnToAngleWithNeg(-60);
				moveForward(10);
			} else {
				// Do nothing
			}
			/*
			 * Starts in the middle Moves forward and delivers gear Moves back
			 * from hook then turns and starts moving to the reloading station
			 */
			break;
		case leftAuto:
			if (color.equals(red)) {
				// Red side left auto
				// Planned starting point = where blue line touches alliance
				// wall
				moveForward(2);
				turnToAngleWithNeg(45);
				dropGear();
				closeDump();
				moveBackward(3.3);
				/*
				 * Moves forward and drops gear on right spike backs up from
				 * spike to fill dump completely
				 */

			} else if (color.equals(blue)) {
				// Blue side left auto
				// Planned Starting point = back right touches blue line and
				// wall
				moveForward(2);
				turnToAngleWithNeg(45);
				moveForward(0.5);
				dropGear();
				moveBackward(3.33);
				openDump();
				turnToAngleWithNeg(5);
				turnToAngleWithNeg(-10);
				turnToAngleWithNeg(10);
				turnToAngleWithNeg(-10);
				turnToAngleWithNeg(10);
				turnToAngleWithNeg(-10);
				/*
				 * moves to left spike and drops gear backs up to boiler and
				 * opens dump wiggles repeatedly to ensure all particles have
				 * escaped
				 */

			} else {
				// Do Nothing
			}
			break;
		case rightAuto:
			if (color.equals(red)) {
				// Red side right auto
				// Planned Starting point = back left touches red line and wall
				moveForward(2);
				turnToAngleWithNeg(-45);
				moveForward(0.5);
				dropGear();
				moveBackward(3.33);
				openDump();
				turnToAngleWithNeg(5);
				turnToAngleWithNeg(-10);
				turnToAngleWithNeg(10);
				turnToAngleWithNeg(-10);
				turnToAngleWithNeg(10);
				turnToAngleWithNeg(-10);
				/*
				 * moves to right spike and drops gear backs up to boiler and
				 * opens dump wiggles repeatedly to ensure all particles have
				 * escaped
				 */

			} else if (color.equals(blue)) {
				// Blue side right auto
				// Planned starting point = where red line touches alliance wall
				moveForward(2);
				turnToAngleWithNeg(-45);
				dropGear();
				closeDump();
				moveBackward(3.3);
				/*
				 * Moves forward and drops gear on left spike backs up from
				 * spike to fill dump completely
				 */

			} else {
				// Do Nothing
			}

			break;
		case defaultAuto:
		default:
			// Do nothing
			break;
		case demo:
			/*
			 * Show boat time, have fun :D
			 */
			turnToAngle(1000000); // Andrei! 1,000,000 degree turn incoming!
			break;
		}
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {

		// Initializations

		// Driver control
		switch (drivingMode) {
		case defaultMode:
		default:
			if (driver.getRawButton(lowGear)) {
				oki.arcadeDrive(lowSpeed * (driver.getRawAxis(driverLeftYAxis)),
						turnSpeed * (driver.getRawAxis(driverRightXAxis)), true);
			} else if (driver.getRawButton(highGear)) {
				oki.arcadeDrive(highSpeed * (driver.getRawAxis(driverLeftYAxis)),
						turnSpeed * (driver.getRawAxis(driverRightXAxis)), true);
			} else {
				oki.arcadeDrive(defaultSpeed * (driver.getRawAxis(driverLeftYAxis)),
						turnSpeed * (driver.getRawAxis(driverRightXAxis)), true);
			}
			break;
		case gtaMode:
			oki.arcadeDrive(driver.getRawAxis(driverRightTrigger) - driver.getRawAxis(driverLeftTrigger),
					turnSpeed * (driver.getRawAxis(driverLeftXAxis)), true);
			break;
		case flightStickMode:
			oki.arcadeDrive(-driver.getRawAxis(driverFlightYAxis), driver.getRawAxis(driverFlightXAxis));
			break;
		}

		// Coil movement
		if (operator.getRawButton(coilIn)) {
			coilMotor.setSpeed(coilSpeed);

		} else if (operator.getRawButton(coilOut)) {
			coilMotor.setSpeed(-coilSpeed);
			rumbleDriver(bothRumble);
		} else {
			coilMotor.setSpeed(0);
			rumbleDriver(noRumble);
		}

		// Dump movement
		if (operator.getPOV() == dumpUp) {
			dumpMotor.setSpeed(dumpSpeed);
			rumbleDriver(rightRumble);
		} else if (operator.getPOV() == dumpDown) {
			dumpMotor.setSpeed(-dumpSpeed);
		} else {
			dumpMotor.setSpeed(0);
			rumbleDriver(noRumble);
		}

		// Winch movement
		if (operator.getRawAxis(winchUp) >= 0.1) {
			winchMotor.setSpeed(operator.getRawAxis(winchUp));
			rumbleDriver(leftRumble);
		} else if (operator.getRawAxis(winchDown) >= 0.1) {
			winchMotor.setSpeed(-operator.getRawAxis(winchDown));
			rumbleDriver(bothRumble);
		} else {
			winchMotor.setSpeed(0);
			rumbleDriver(noRumble);
		}

	}

	/**
	 * Move forward for given time Approximately x meters / second 1s = 1000ms
	 */
	private void moveForward(double timeInSeconds) {
		timer.reset();
		timer.start();
		if (timer.get() <= timeInSeconds) {
			oki.arcadeDrive(defaultSpeed, -gyro.getAngle() * Kp);
		} else {
		}
	}

	/**
	 * Move backward for given time Approximately x meters / second 1s = 1000ms
	 */
	private void moveBackward(double timeInSeconds) {
		timer.reset();
		timer.start();
		if (timer.get() <= timeInSeconds) {
			oki.arcadeDrive(-defaultSpeed, -gyro.getAngle() * Kp);
		} else {
		}
	}

	/*
	 * Turn to given angle
	 */
	private void turnToAngle(int angle) {
		gyro.reset();
		if (angle > 180) {
			while (gyro.getAngle() != angle) {
				oki.arcadeDrive(0, -turnSpeed);
			}
		} else if (angle <= 180) {
			while (gyro.getAngle() != angle) {
				oki.arcadeDrive(0, turnSpeed);
			}
		}
	}

	private void turnToAngleWithNeg(int angle) {
		gyro.reset();
		oki.arcadeDrive(0, turnSpeed * (angle - gyro.getAngle()) * Kp);
	}

	/*
	 * Drops gear in autonomous
	 */
	private void dropGear() {
		timer.reset();
		timer.start();
		while (timer.get() < dropCoilTime) {
			coilMotor.setSpeed(coilSpeed);
		}
		coilMotor.setSpeed(0);
	}

	/*
	 * Opens dump until limit switch is activated
	 */
	private void openDump() {
		while (upperLimit.get() == false) {
			dumpMotor.setSpeed(dumpSpeed);
		}
		dumpMotor.setSpeed(0);
	}

	/*
	 * Closes dump until limit switch is activated
	 */
	private void closeDump() {
		while (lowerLimit.get() == false) {
			dumpMotor.setSpeed(-dumpSpeed);
		}
		dumpMotor.setSpeed(0);
	}

	// Driver Rumble
	private void rumbleDriver(int rumbleMode) {
		switch (rumbleMode) {
		case leftRumble:
			driver.setRumble(RumbleType.kLeftRumble, 1);
			break;
		case rightRumble:
			driver.setRumble(RumbleType.kRightRumble, 1);
			break;
		case bothRumble:
			driver.setRumble(RumbleType.kLeftRumble, 1);
			driver.setRumble(RumbleType.kRightRumble, 1);
			break;
		case noRumble:
		default:
			driver.setRumble(RumbleType.kLeftRumble, 0);
			driver.setRumble(RumbleType.kRightRumble, 0);
			break;
		}
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}
