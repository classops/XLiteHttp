package com.hanter.litehttp.task;

import com.hanter.litehttp.RequestTaskRunnable;
import com.hanter.litehttp.utils.LiteHttpConfig;
import com.hanter.litehttp.utils.LiteHttpLogger;

/**
 * 类名：BlockingTaskThread <br/>
 * 描述：取消堵塞方法的线程，线程池对象 <br/>
 * 创建时间：2016年2月19日 上午10:48:28
 * @author wangmingshuo@ddsoucai.cn
 * @version 1.0
 */
public class BlockingTaskThread extends Thread {

	private final static String TAG = "BlockingTaskThread";
	
	private Runnable mRunnable;

	public BlockingTaskThread(ThreadGroup group, Runnable target, String name,
		   long stackSize) {
		super(group, target, name, stackSize);
		this.mRunnable = target;
	}

	@Override
	public void interrupt() {
		// 处理 不可中断堵塞方法的代码
		// Socket I/O 中断

		LiteHttpLogger.d(LiteHttpConfig.TASK_THREAD_DEBUG, TAG,  getName() + " interrupt");
		
		try {
			if (mRunnable instanceof RequestTaskRunnable) {
				((RequestTaskRunnable) mRunnable).interrupt();
			}
		} catch (Exception e) {
			// 捕捉异常
			e.printStackTrace();
		} finally {
			super.interrupt();
		}
	
	}
	
}
