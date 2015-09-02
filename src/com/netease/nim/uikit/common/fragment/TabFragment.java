package com.netease.nim.uikit.common.fragment;

public abstract class TabFragment extends TFragment {

	public interface State {
		public boolean isCurrent(TabFragment fragment);
	}

	private State state;

	public void setState(State state) {
		this.state = state;
	}

	/**
	 * is current
	 * 
	 * @return
	 */
	protected final boolean isCurrent() {
		return state.isCurrent(this);
	}

	/**
	 * notify current
	 */
	public void onCurrent() {
		// NO OP
	}

	/**
	 * leave current page
	 */
	public void onLeave() {

	}

	/**
	 * notify current scrolled
	 */
	public void onCurrentScrolled() {
		// NO OP
	}

	public void onCurrentTabClicked() {
		// NO OP
	}
	
	public void onCurrentTabDoubleTap(){
		// NO OP
	}
}
