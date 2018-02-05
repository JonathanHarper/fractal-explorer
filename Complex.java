
//Creating new Complex numbers
public class Complex {

	//Real part
	private double real;
	//Imaginary part
	private double imaginary;
	
	protected Complex(double real, double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public double getReal() {
		return real;
	}
	
	public double getImaginary() {
		return imaginary;
	}
	
	public void setReal(double r) {
		real = r;
	}
	
	public void setImaginary(double i) {
		imaginary = i;
	}
	
	//Modulus (magnitude) of the complex number
    public double modulus() {
        return Math.sqrt((real * real) + (imaginary * imaginary));
    }
	
	//Squares the complex number. (a + bi)^2 where (bi)^2 = -1b^2
	public Complex square() {
		double real = (this.real * this.real) - (this.imaginary * this.imaginary);
		double imaginary = (this.real * this.imaginary) * 2;
		
		return new Complex(real, imaginary);
	}
	
	//Finds the modulus squared of the Complex number.
	public double modulusSquared() {	
		return real*real + imaginary * imaginary;
	}
	
	//Adds this Complex number together with a specified one 
	public Complex add(Complex c) {
		return new Complex(this.real + c.real, this.imaginary + c.imaginary);
	}
	
}
