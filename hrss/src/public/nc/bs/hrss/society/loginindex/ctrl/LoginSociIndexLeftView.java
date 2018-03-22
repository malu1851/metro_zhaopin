package nc.bs.hrss.society.loginindex.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.hrss.pub.HrssConsts;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.hrss.pub.tool.ViewUtil;
import nc.hr.utils.ResHelper;
import nc.itf.rm.IRMPsndocManageService;
import nc.itf.rm.IRMPsndocQueryService;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.cache.LfwCacheManager;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.cmd.UifOpenViewCmd;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.ScriptEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.common.ResumeSourceEnum;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author chouhl
 */
public class LoginSociIndexLeftView implements IController {
	private static final long serialVersionUID = 1L;

	private static final String WIN_SCTY_RESUME = "SctyResume";
	private static final String WIN_SCHL_RESUME = "schlResume";
	public static final String WIDTH = "100%";
	public static final String HEIGHT = "680";
	public void onDataLoad(DataLoadEvent dataLoadEvent) {
		Dataset ds = dataLoadEvent.getSource();
		queryRMPsndocVOByPk(ds);
	}

	/**
	 * 查询当前用户申请的职位
	 *
	 * @param ds
	 */
	public void queryRMPsndocVOByPk(Dataset ds){
		SessionBean session = SessionUtil.getRMWebSessionBean();
		RmUserVO rmUserVO = session.getRmUserVO();

		try {
			showPostDetail(null);
			AggRMPsndocVO aggRMPsndocVO = ServiceLocator.lookup(IRMPsndocQueryService.class).queryByPK(
					rmUserVO.getHrrmpsndoc());
			if (aggRMPsndocVO == null)
				return;
			CircularlyAccessibleValueObject[] superVOs = aggRMPsndocVO.getTableVO("rm_psndoc_job");
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			if (ArrayUtils.isEmpty(superVOs))
				return;
			for (CircularlyAccessibleValueObject superVO : superVOs) {
				if (RMPsnJobVO.ABSTRACTJOB.equals(((RMPsnJobVO)superVO).getPk_reg_job())){
					continue;
				}
				// 只显示校招和社招的职位
				if(ResumeSourceEnum.PLATFORM_SCH.toIntValue()!=((RMPsnJobVO)superVO).getSourcetype() 
						&& ResumeSourceEnum.PLATFORM_SOC.toIntValue()!=((RMPsnJobVO)superVO).getSourcetype()){
				     continue;
				}
				superVOlist.add((SuperVO) superVO);
			}
			new SuperVO2DatasetSerializer().serialize(superVOlist.toArray(new SuperVO[0]), ds, Row.STATE_NORMAL);
		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}
	}

	/**
	 * 简历维护
	 *
	 * @param scriptEvent
	 */
	public void showPostDetail(ScriptEvent scriptEvent) {
		// 职位主键
		String pk_job = (String) LfwRuntimeEnvironment.getWebContext().getPageMeta().getExtendAttributeValue("pk_job");
		SessionBean bean = SessionUtil.getRMWebSessionBean();
		if (StringUtils.isEmpty(pk_job) || !StringUtils.isEmpty(bean.getRmUserVO().getHrrmpsndoc()))
			return;
		AppLifeCycleContext.current().getApplicationContext().addAppAttribute("pk_job", pk_job);
		showResumeWindow();

	}

	/**
	 * 简历维护
	 *
	 * @param scriptEvent
	 */
	public void updresume(ScriptEvent scriptEvent) {
		showResumeWindow();
	}
	
	/**
	 * 打开简历窗口
	 * 
	 */
	private void showResumeWindow(){
		
		
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		
		//获取前台jsp界面的type来区分校招和社招，type=1 社招，校招
		int  interviewtype = unLoginBean.getType();
		
		if(interviewtype !=1&&interviewtype !=2){
			
			CommonUtil.showMessageDialog("获取界面数据数据异常");	
		}
		
		if (interviewtype == 1) {
			CommonUtil.showWindowDialog(WIN_SCTY_RESUME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res "请完善简历"*/, WIDTH, HEIGHT, null, ApplicationContext.TYPE_DIALOG,false,true);
		} else if(interviewtype == 2) {
			CommonUtil.showWindowDialog(WIN_SCHL_RESUME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res "请完善简历"*/, WIDTH, HEIGHT, null, ApplicationContext.TYPE_DIALOG,false,true);
		}
	}
	
	/**
	 * 上传照片
	 * 
	 * @param scriptEvent
	 */
	public void updphoto(ScriptEvent scriptEvent){
		SessionBean bean = SessionUtil.getRMWebSessionBean();
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		RmUserVO rmUserVO = bean.getRmUserVO();
		String pk_psndoc = rmUserVO.getHrrmpsndoc();
		
		// 如果还没有填写简历，则跳转到简历注册页面
		if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
			if (unLoginBean.getBrower_jobtyle() == 1) {
				CommonUtil.showWindowDialog("SctyResume", nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res "请完善简历"*/, 
						LoginSociIndexLeftView.WIDTH, LoginSociIndexLeftView.HEIGHT, null, ApplicationContext.TYPE_DIALOG,false,true);
			} else {
				CommonUtil.showWindowDialog("schlResume", nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res "请完善简历"*/,
						LoginSociIndexLeftView.WIDTH, LoginSociIndexLeftView.HEIGHT, null, ApplicationContext.TYPE_DIALOG,false,true);
			}
		} else {
			String dialogUrl = LfwRuntimeEnvironment.getRootPath() + "/core/file.jsp?pageId=file&sysid=bafile&fileExt=*.jpg;*.png;*.gif&fileDesc=image&filemanager=nc.bs.hrss.society.loginindex.RMFileManager&allmethod=afterFileUpload";
			getAppContext().showModalDialog(dialogUrl, ResHelper.getString("c_pub-res","0c_pub-res0054")/*@res "上传文件"*/, "450", "450", "", "");
		}
	}
	
	private ApplicationContext getAppContext(){
		return AppLifeCycleContext.current().getApplicationContext();
	}

	/**
	 * 修改密码
	 *
	 * @param scriptEvent
	 */
	public void updpassword(ScriptEvent scriptEvent) {
		CmdInvoker.invoke(new UifOpenViewCmd("password", "370", "300", nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0026")/*@res "修改密码"*/));
	}

	/**
	 * 安全退出
	 *
	 * @param scriptEvent
	 */
	public void logout(ScriptEvent scriptEvent) {
		LfwCacheManager.getSessionCache().remove(HrssConsts.RMWEBSESSION_BEAN_ID);
		ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
		SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
		String url = null;
		if(bean.getBrower_jobtyle() == HrssConsts.RMWEBJOBTYPE_SCHOOL){
			url = LfwRuntimeEnvironment.getRootPath() + "/app/RMWebSchoolIndexApp";
		}else{
			url = LfwRuntimeEnvironment.getRootPath() + "/app/RMWebSociIndexApp";
		}
		appCtx.sendRedirect(url);
	}

	/**
	 * 返回主页
	 *
	 * @param scriptEvent
	 */
	public void toindex(ScriptEvent scriptEvent) {
		ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
		String app = "/app/RMWebLoginSociIndexApp";
		String url = LfwRuntimeEnvironment.getRootPath() + app;
		appCtx.sendRedirect(url);
	}

	/**
	 * 返回活动页面
	 *
	 * @param scriptEvent
	 */
	public void toActivePage(ScriptEvent scriptEvent) {
		ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + "/app/RMWebSchoolIndexApp";
		appCtx.sendRedirect(url);
	}

	@SuppressWarnings("rawtypes")
	public void plugininid_soci(Map keys) {
		toindex(null);
	}
	
	
	@SuppressWarnings("rawtypes")
	public void plugininid_schl(Map keys) {
		toindex(null);
	}
	

	/**
	 * 刷新职位申请列表
	 *
	 * @param keys
	 */
	@SuppressWarnings("rawtypes")
	public void pluginApproveJob(Map keys) {
		LfwView viewMain = AppLifeCycleContext.current().getViewContext().getView();
		if (viewMain == null) {
			return;
		}
		Dataset ds = viewMain.getViewModels().getDataset("rm_psndoc_job");
		queryRMPsndocVOByPk(ds);
	}
	/**
	 * 删除申请的职位
	 * @param scriptEvent
	 */
	public void deleteJob(ScriptEvent scriptEvent) {
		//要删除职位的 pk
		String primarykey = AppLifeCycleContext.current().getParameter("dsLeft_primaryKey");
		Dataset ds = ViewUtil.getDataset(ViewUtil.getView("left"), "rm_psndoc_job");
		
		//删除时弹出提示框是否确认删除
		if(! showConfirmDialog(/*ResHelper.getString("node_rmweb-res", "w_rmweb-000214")@res"您确定要删除所选数据"*/"您确定要删除所选数据")){
			return;
		}
		
		
		IRMPsndocManageService service = null;
		try {
			service = ServiceLocator.lookup(IRMPsndocManageService.class);
			service.delPsnJob(primarykey);
			CommonUtil.showShortMessage("删除成功");			
		} catch (HrssException e) {
			e.alert();
		} catch (BusinessException e) {
			new HrssException(e);
		}
		
		//删除成功之后刷新一下职位列表
		queryRMPsndocVOByPk(ds);
		
	}
	
	/**
	 * 确认删除对话框
	 * @param msg
	 * @return
	 */
	private boolean showConfirmDialog(String msg){
//		return CommonUtil.showConfirmDialog(ResHelper.getString("node_rmweb-res", "w_rmweb-000213")/*@res"确认对话"*/, msg);
		return CommonUtil.showConfirmDialog("确认对话", msg);
	}
	
	
}
