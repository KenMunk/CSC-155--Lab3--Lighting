package a3;

public class AxisState{
	
	int upCode;
	int downCode;
	private boolean upState;
	private boolean downState;
	private float multiplier;
	
	public AxisState(float multiplier, int upCode, int downCode){
		this.upCode = upCode;
		this.downCode = downCode;
		this.upState = false;
		this.downState = false;
		this.multiplier = multiplier;
	}
	
	public void pressCheck(int keycode){
		
		if(keycode == this.upCode){
			this.pressUp();
		}
		if(keycode == this.downCode){
			this.pressDown();
		}
		
	}
	
	public void releaseCheck(int keycode){
		
		if(keycode == this.upCode){
			this.releaseUp();
		}
		if(keycode == this.downCode){
			this.releaseDown();
		}
		
	}
	
	private void pressUp(){
		this.upState = true;
	}
	
	private void releaseUp(){
		this.upState = false;
	}
	
	private void pressDown(){
		this.downState = true;
	}
	
	private void releaseDown(){
		this.downState = false;
	}
	
	public float getValue(){
		float value = (this.downState ^ this.upState) ? this.multiplier : 0;
		
		value *= this.downState ? -1 : 1;
		
		return(value);
		
	}
	
}