package com.android.launcher3;

import java.util.ArrayList;

import android.view.View;

import com.android.launcher3.much.ShakeAnimationManager;

public class MuchAppShakeAndShareManager {
	public  ShakeState mDeleteState = ShakeState.DELETE_NONE;

	private static MuchAppShakeAndShareManager mInstance;
	private Workspace mWorkspace;

    public enum ShakeState {
        DELETE_NONE, DELETE_DESKTOP, DELETE_FOLDER
    }

	private MuchAppShakeAndShareManager(){

	}

	public static MuchAppShakeAndShareManager getInstance(){
		if(mInstance == null){
			synchronized (MuchAppShakeAndShareManager.class) {
				if(mInstance == null){
					mInstance = new MuchAppShakeAndShareManager();
				}
			}
		}
		return mInstance;
	}

	public void setWorkspace(Workspace workspace){
		mWorkspace = workspace;
	}

	public ShakeState getDeleteState() {
		return mDeleteState;
	}

	public void handleClickToShake(boolean isLongClick, boolean isShortcutIcon){
        if(isLongClick){
            if(isShortcutIcon){
                if (mDeleteState == ShakeState.DELETE_NONE) {
                    setDeleteState(ShakeState.DELETE_DESKTOP);
                    toShake(ShakeState.DELETE_DESKTOP,true);
                }
            }else{
                shakeAll();
            }
        }else{
            if(isShortcutIcon){
                shakeAll();
            }else{
                if (mDeleteState == ShakeState.DELETE_DESKTOP) {
                    toShakeOpenFolder(ShakeState.DELETE_DESKTOP,true);
                }
            }
        }
    }

	private void shakeAll() {
        if (mDeleteState == ShakeState.DELETE_NONE) {
            setDeleteState(ShakeState.DELETE_DESKTOP);
            toShake(ShakeState.DELETE_DESKTOP,true);
            toShakeOpenFolder(ShakeState.DELETE_DESKTOP,true);
        }
    }

	public void setDeleteState(ShakeState state){
        mDeleteState = state;
     }
     /**
      * 抖动当前布局中的所有图标
      * @param state
      * @param isToDelete
      */
    private void toShake(ShakeState state,boolean isToDelete) {
//    	if(mWorkspace == null){
//    		mDeleteState = ShakeState.DELETE_NONE;
//    		return ;
//    	}
        if (state == ShakeState.DELETE_DESKTOP) {
            ArrayList<ShortcutAndWidgetContainer> childrenLayouts = mWorkspace.getAllShortcutAndWidgetContainers();
            for (ShortcutAndWidgetContainer layout : childrenLayouts) {
                int childCount = layout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = layout.getChildAt(i);
                    toShake(child, isToDelete);
                }
            }
        }
        if (state == ShakeState.DELETE_FOLDER) {
            Folder curFolder = mWorkspace.getOpenFolder();
            if (curFolder != null) {
                ArrayList<View> arrays = curFolder.getItemsInReadingOrder();
                for (int i = 0; i < arrays.size(); i++) {
                    final View view = arrays.get(i);
                    toShake(view, isToDelete);
                }
            }
        }
    }

    void toShakeOpenFolder(ShakeState state,boolean isToDelete){
        if (state == ShakeState.DELETE_DESKTOP) {
            Folder curFolder = mWorkspace.getOpenFolder();
            if (curFolder != null) {
                ArrayList<View> arrays = curFolder.getItemsInReadingOrder();
                for (int i = 0; i < arrays.size(); i++) {
                    final View view = arrays.get(i);
                    toShake(view, isToDelete);
                }
            }
        }
    }
    /**
      * 抖动单个图标
      * @param state
      * @param isToDelete
      */
    void toShake(View view,boolean isShake){
        ShakeAnimationManager manager = ShakeAnimationManager.getInstance();
        if (view instanceof BubbleTextView) {
            BubbleTextView bubbleTextView = (BubbleTextView)view;
            if (!bubbleTextView.isCanEdit()) {
                bubbleTextView.getDeleteRect().setDelete(false);
                isShake = false;
            }else {
                bubbleTextView.getDeleteRect().setDelete(isShake);
            }
            if(isShake) {
                manager.startShakeAnim(view);
            }
         }else if(view instanceof FolderIcon){
             FolderIcon folderIcon = (FolderIcon)view;
             folderIcon.getDeleteRect().setDelete(isShake);
             if(isShake) manager.startShakeAnim(view);
         }else if(view instanceof LauncherAppWidgetHostView){
             LauncherAppWidgetHostView widget = (LauncherAppWidgetHostView)view;
             widget.getDeleteRect().setDelete(isShake);
             if(isShake) {
                 manager.startHeartbeatAnim(view);
             }
          }
        if(!isShake){
            manager.stopAnim(view);
        }
        view.invalidate();
    }

    public void stopShakeAnim() {
        if (mDeleteState == ShakeState.DELETE_DESKTOP) {
            toShake(ShakeState.DELETE_DESKTOP,false);
            toShakeOpenFolder(ShakeState.DELETE_DESKTOP, false);
            setDeleteState(ShakeState.DELETE_NONE);
        }
    }

    public boolean unStartApp(){
    	return mDeleteState != ShakeState.DELETE_NONE;
    }
}
