package com.halohoop.usoppbubble.widget;

/**
 * Created by Pooholah on 2017/6/1.
 */

public interface DraggableListener {
    void onBubbleDragStart(UsoppBubble view);
    /**
     * 放手发射了
     */
    void onOnBubbleReleaseWithLaunch(UsoppBubble view);

    /**
     * 放手没有发射
     */
    void onOnBubbleReleaseWithoutLaunch(UsoppBubble view);
//        void onOnBubbleDragging();//may be later
}
