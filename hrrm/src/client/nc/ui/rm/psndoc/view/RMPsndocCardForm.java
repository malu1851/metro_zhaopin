package nc.ui.rm.psndoc.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.rm.IRMPsndocQueryMaintain;
import nc.ui.cp.cpindi.ref.CPindiGradeRefModel;
import nc.ui.hr.managescope.ref.MsDeptRefModel2;
import nc.ui.hr.uif2.view.HrBillFormEditor;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.pub.bill.BillCardLayout;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillEditListener2;
import nc.ui.pub.bill.BillModel;
import nc.ui.rm.psndoc.model.RMPsndocAppModel;
import nc.ui.rm.psnlib.model.PsnLibAppModel;
import nc.ui.rm.pub.RMRefModelWherePartUtils;
import nc.ui.rm.ref.PublishJobRefModel;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.UIState;
import nc.ui.uif2.model.AppEventConst;
import nc.vo.hr.managescope.ManagescopeBusiregionEnum;
import nc.vo.pub.BusinessException;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnCPVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.publish.PublishJobVO;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 应聘登记人员　卡片界面
 * @author yucheng
 *
 */
@SuppressWarnings("serial")
public class RMPsndocCardForm extends HrBillFormEditor implements BillEditListener2{
	
	private IRMPsndocQueryMaintain queryMaintain;
	@Override
	public void handleEvent(AppEvent event){
		super.handleEvent(event);
		
		if (AppEventConst.SELECTION_CHANGED == event.getType()||AppEventConst.MULTI_SELECTION_CHANGED == event.getType()) {
			// 卡片界面时，不是编辑态的时候，赋值图片
			if ( getModel().getUiState() == UIState.NOT_EDIT) {
				RMPsndocAppModel md =(RMPsndocAppModel)getModel();
				AggRMPsndocVO selectData = (AggRMPsndocVO) md.getSelectedData();
				md.syncPhoto(selectData);
				setValue(md.getSelectedData());				
			}
						
		} else if (AppEventConst.SELECTED_DATE_CHANGED == event.getType()) {
			// 刷新、启动、关闭
			// 卡片界面时，不是编辑态的时候，赋值图片isComponentVisible() &&
			if ( getModel().getUiState() == UIState.NOT_EDIT) {
				RMPsndocAppModel md =(RMPsndocAppModel)getModel();
				AggRMPsndocVO selectData = (AggRMPsndocVO) md.getSelectedData();
				md.syncPhoto(selectData);
				setValue(md.getSelectedData());
			}
		}
		
		
		
	}	
	@Override
	public void initUI() {
		super.initUI();
		getBillCardPanel().addBodyEditListener2(RMPsnJobVO.getDefaultTableName(), this);
		getBillCardPanel().addBodyEditListener2(RMPsnCPVO.getDefaultTableName(), this);
		// 设置界面比例主表大小为55%
		((BillCardLayout)getBillCardPanel().getLayout()).setHeadScale(70);
	}

	@Override
	public boolean beforeEdit(BillEditEvent e) {
		BillModel billModel = getBillCardPanel().getBillModel(e.getTableCode());
		//编辑用人组织
		if(RMPsnJobVO.PK_REG_ORG.equals(e.getKey())){
			UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_ORG).getComponent();
			//不应受平台的数据权限控制
			refPane.getRefModel().setUseDataPower(false);
			refPane.getRefModel().addWherePart(RMRefModelWherePartUtils.getMsOrgWherePart(getModel().getContext().getPk_org()));
		}
		// 编辑应聘部门
		if(RMPsnJobVO.PK_REG_DEPT.equals(e.getKey())){
			DefaultConstEnum rmOrg = (DefaultConstEnum)billModel.getValueObjectAt(e.getRow(), RMPsnJobVO.PK_REG_ORG);
			UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_DEPT).getComponent();
			MsDeptRefModel2 refmodel = (MsDeptRefModel2)refPane.getRefModel();
			refmodel.setBusiregionEnum(ManagescopeBusiregionEnum.invite);
			refmodel.setPk_hrorg(getModel().getContext().getPk_org());
			String rmorg = null;
			if(rmOrg!=null&&rmOrg.getValue()!=null){
				rmorg = (String)rmOrg.getValue();
			}
			refmodel.addWherePart(RMRefModelWherePartUtils.getMsDeptWherePart(rmorg, getModel().getContext().getPk_org()));
			refPane.setRefModel(refmodel);
			
		}
		// 编辑应聘职位
		if(RMPsnJobVO.PK_REG_JOB.equals(e.getKey())){
			DefaultConstEnum rmOrg = (DefaultConstEnum)billModel.getValueObjectAt(e.getRow(), RMPsnJobVO.PK_REG_ORG);
			DefaultConstEnum rmDept= (DefaultConstEnum)billModel.getValueObjectAt(e.getRow(), RMPsnJobVO.PK_REG_DEPT);
			PublishJobRefModel refModel = (PublishJobRefModel) ((UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_JOB).getComponent()).getRefModel();
			refModel.setPk_rmorg(rmOrg==null?null:(String)rmOrg.getValue());
			refModel.setPk_rmdept(rmDept==null?null:(String)rmDept.getValue());
		}
		if(RMPsnJobVO.PK_ACTIVE.equals(e.getKey())){
			UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_ACTIVE).getComponent();
			refPane.getRefModel().addWherePart(" and "+ActiveVO.ACTIVESTATE + " = 1 ");
		}
		// 编辑达到等级
		if(RMPsnCPVO.PK_GRADEREACH.equals(e.getKey())){
			DefaultConstEnum pk_indi = (DefaultConstEnum)billModel.getValueObjectAt(e.getRow(), RMPsnCPVO.PK_INDI);
			UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnCPVO.PK_GRADEREACH).getComponent();
			((CPindiGradeRefModel)refPane.getRefModel()).setPk_indi(pk_indi==null?null:(String)pk_indi.getValue());
		}
		return true;
	}
	
	@Override
	public void afterEdit(BillEditEvent evt) {
		super.afterEdit(evt);
		// 应聘职位表编辑后事件
		if(RMPsnJobVO.getDefaultTableName().equals(evt.getTableCode())){
			BillModel billModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
			int row = evt.getRow();
			// 修改了应聘组织，清空应聘部门和应聘职位
			if(RMPsnJobVO.PK_REG_ORG.equals(evt.getKey())){
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_JOB);
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_DEPT);
			}
			// 修改了应聘部门，清空应聘职位
			else if(RMPsnJobVO.PK_REG_DEPT.equals(evt.getKey())){
				//billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_JOB);
				UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_DEPT).getComponent();
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_ORG);
				billModel.setValueAt((String)refPane.getRefValue("pk_father"), row, RMPsnJobVO.PK_REG_ORG);
			}
			// 修改了应聘职位, 设置招聘职位、应聘组织、应聘部门
			else if(RMPsnJobVO.PK_REG_JOB.equals(evt.getKey()) && evt.getValue()!=null){
				UIRefPane refPane = (UIRefPane) getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName()).getItemByKey(RMPsnJobVO.PK_REG_JOB).getComponent();
				RMPsnJobVO jobVO = (RMPsnJobVO) billModel.getBodyValueRowVO(row, RMPsnJobVO.class.getName());
				jobVO.setPk_reg_org((String) refPane.getRefValue(PublishJobVO.PK_RMORG));
				jobVO.setPk_reg_dept((String) refPane.getRefValue(PublishJobVO.PK_RMDEPT));
				jobVO.setPk_jobsource((String) refPane.getRefValue(PublishJobVO.PK_JOB));
				jobVO.setPk_active((String)refPane.getRefValue(PublishJobVO.PK_ACTIVITY));
				jobVO.setPk_channel((String)refPane.getRefValue(PublishJobVO.PK_CHANNEL));
				billModel.setBodyRowObjectByMetaData(jobVO, row);
				// 同步能力素质
				//synPsnCapaTable();
				synPsnCapaInfo();
			}
		}
	}
	public void synPsnCapaInfo(){
		//收集应聘职位页签中职位发布主键jobList
		BillModel jobBillModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
		int jobSize = jobBillModel.getRowCount();
		List<String> jobList =  new ArrayList<String>();
		for(int i = 0;i < jobSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnJobVO.PK_REG_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || jobList.contains(pk_publishjob))
				continue;
			jobList.add(pk_publishjob);//应聘职位页签，职位发布主键集合
		}
		//收集能力素质页签中职位发布主键cpList
		BillModel cpBillModel = getBillCardPanel().getBillModel(RMPsnCPVO.getDefaultTableName());
		int cpSize = cpBillModel.getRowCount();
		List<String> cpList =  new ArrayList<String>();
		for(int i = 0;i < cpSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) cpBillModel.getValueObjectAt(i, RMPsnCPVO.PK_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
//			if(StringUtils.isEmpty(pk_publishjob) || cpList.contains(pk_publishjob))
//				continue;
			cpList.add(pk_publishjob);//应聘职位页签，职位发布主键集合
		}
		//需要新增cp的publish集合
		List<String> addList = new ArrayList<String>();
		List<Integer> delList = new ArrayList<Integer>();
		//查找要新增cp的publish主键
		for(int i=0;i<jobList.size();i++){
			boolean flag = false;
			for(int j=0;j<cpList.size();j++){
				if(jobList.get(i).equals(cpList.get(j)))
					flag=true;
			}
			if(!flag)
				addList.add(jobList.get(i));
		}
		for(int i=0;i<cpList.size();i++){
			boolean flag = false;
			for(int j=0;j<jobList.size();j++){
				if(cpList.get(i).equals(jobList.get(j)))
					flag=true;
			}
			if(!flag)
				delList.add(i);
		}
		// 删除指标
		if(!CollectionUtils.isEmpty(delList))
			cpBillModel.delLine(ArrayUtils.toPrimitive(delList.toArray(new Integer[0])));
		// 新增指标
		if(CollectionUtils.isEmpty(addList))
			return;
		Map<String, RMPsnCPVO[]> psnCPMap = null;
		try {
			psnCPMap = getQueryMaintain().queryPsnCPVOByJobPks(addList.toArray(new String[0]));
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		if(MapUtils.isEmpty(psnCPMap))
			return;
		// 新增指标行并赋值
		for(String key:psnCPMap.keySet()){
			RMPsnCPVO[] cpVOs = psnCPMap.get(key);
			if(ArrayUtils.isEmpty(cpVOs))
				continue;
			cpSize = cpBillModel.getRowCount();
			cpBillModel.addLine(cpVOs.length);
			cpBillModel.setBodyRowObjectByMetaData(cpVOs, cpSize);
		}
	}
	
	/**
	 * 应聘职位变化后同步能力素质子集
	 */
	public void synPsnCapaTable(){
		//收集应聘职位页签中职位发布主键jobList
		//收集能力素质页签中职位发布主键cpList
		//嵌套循环jobList,cpList 记录
		BillModel jobBillModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
		int jobSize = jobBillModel.getRowCount();
		// 取当前界面上的所有应聘职位主键，需要去重
		List<String> allJobs = new ArrayList<String>();
		for(int i = 0;i < jobSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnJobVO.PK_REG_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || allJobs.contains(pk_publishjob))
				continue;
			allJobs.add(pk_publishjob);//应聘职位页签，职位发布主键集合
		}
		BillModel cpBillModel = getBillCardPanel().getBillModel(RMPsnCPVO.getDefaultTableName());
		int cpSize = cpBillModel.getRowCount();
		// 待删除的行
		List<Integer> delRowList = new ArrayList<Integer>();
		// 已有职位主键
		List<String> existJobs = new ArrayList<String>();
		// 循环处理指标子集，处理完成后delRowList中为要删除的指标行，allJobs中为要从数据库中查询出来放到指标界面上的职位
		for(int i = 0;i < cpSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnCPVO.PK_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || !(allJobs.contains(pk_publishjob) || existJobs.contains(pk_publishjob))){
				delRowList.add(i);
				continue;
			}
			// 如果已有职位主键中已存在则不再处理
			if(existJobs.contains(pk_publishjob))
				continue;
			// 否则要在所有职位主键中删除此主键并加入到已有职位主键中
			allJobs.remove(pk_publishjob);
			existJobs.add(pk_publishjob);
		}
		// 删除指标
		if(!CollectionUtils.isEmpty(delRowList))
			cpBillModel.delLine(ArrayUtils.toPrimitive(delRowList.toArray(new Integer[0])));
		// 新增指标
		if(CollectionUtils.isEmpty(allJobs))
			return;
		Map<String, RMPsnCPVO[]> psnCPMap = null;
		try {
			psnCPMap = getQueryMaintain().queryPsnCPVOByJobPks(allJobs.toArray(new String[0]));
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		if(MapUtils.isEmpty(psnCPMap))
			return;
		// 新增指标行并赋值
		for(String key:psnCPMap.keySet()){
			RMPsnCPVO[] cpVOs = psnCPMap.get(key);
			if(ArrayUtils.isEmpty(cpVOs))
				continue;
			cpSize = cpBillModel.getRowCount();
			cpBillModel.addLine(cpVOs.length);
			cpBillModel.setBodyRowObjectByMetaData(cpVOs, cpSize);
		}
	}

	@Override
	public Object getValue() {
		AggRMPsndocVO aggVO = (AggRMPsndocVO) super.getValue();
		aggVO.getPsndocVO().setPk_group(getModel().getContext().getPk_group());
		aggVO.getPsndocVO().setPk_org(getModel().getContext().getPk_org());
		// 设置应聘职位数
		AggRMPsndocVO showVO = (AggRMPsndocVO) billCardPanel.getBillData().getBillObjectByMetaData();	//页面看到的数据（除了已删除的数据）
		aggVO.getPsndocVO().setJobsize(ArrayUtils.getLength(showVO.getTableVO(RMPsnJobVO.getDefaultTableName())));
		return aggVO;
	}

	public void setComponentVisible(boolean visible){
		// 当显示状态未改变时不重新设置显示状态，否则会触发列表卡片页签同步监听
		if(isComponentVisible()==visible)
			return;
		super.setComponentVisible(visible);
	}
	
	@Override
	public void showMeUp() {
		super.showMeUp();
		if(getModel() instanceof RMPsndocAppModel)
			((RMPsndocAppModel)getModel()).setCardShow(true);
		if( getModel() instanceof PsnLibAppModel){
			((PsnLibAppModel)getModel()).setCardShow(true);
		}
	}

	public IRMPsndocQueryMaintain getQueryMaintain() {
		if(queryMaintain==null)
			queryMaintain = NCLocator.getInstance().lookup(IRMPsndocQueryMaintain.class);
		return queryMaintain;
	}

}
