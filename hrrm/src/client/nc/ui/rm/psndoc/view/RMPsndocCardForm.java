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
 * ӦƸ�Ǽ���Ա����Ƭ����
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
			// ��Ƭ����ʱ�����Ǳ༭̬��ʱ�򣬸�ֵͼƬ
			if ( getModel().getUiState() == UIState.NOT_EDIT) {
				RMPsndocAppModel md =(RMPsndocAppModel)getModel();
				AggRMPsndocVO selectData = (AggRMPsndocVO) md.getSelectedData();
				md.syncPhoto(selectData);
				setValue(md.getSelectedData());				
			}
						
		} else if (AppEventConst.SELECTED_DATE_CHANGED == event.getType()) {
			// ˢ�¡��������ر�
			// ��Ƭ����ʱ�����Ǳ༭̬��ʱ�򣬸�ֵͼƬisComponentVisible() &&
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
		// ���ý�����������СΪ55%
		((BillCardLayout)getBillCardPanel().getLayout()).setHeadScale(70);
	}

	@Override
	public boolean beforeEdit(BillEditEvent e) {
		BillModel billModel = getBillCardPanel().getBillModel(e.getTableCode());
		//�༭������֯
		if(RMPsnJobVO.PK_REG_ORG.equals(e.getKey())){
			UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_ORG).getComponent();
			//��Ӧ��ƽ̨������Ȩ�޿���
			refPane.getRefModel().setUseDataPower(false);
			refPane.getRefModel().addWherePart(RMRefModelWherePartUtils.getMsOrgWherePart(getModel().getContext().getPk_org()));
		}
		// �༭ӦƸ����
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
		// �༭ӦƸְλ
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
		// �༭�ﵽ�ȼ�
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
		// ӦƸְλ��༭���¼�
		if(RMPsnJobVO.getDefaultTableName().equals(evt.getTableCode())){
			BillModel billModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
			int row = evt.getRow();
			// �޸���ӦƸ��֯�����ӦƸ���ź�ӦƸְλ
			if(RMPsnJobVO.PK_REG_ORG.equals(evt.getKey())){
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_JOB);
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_DEPT);
			}
			// �޸���ӦƸ���ţ����ӦƸְλ
			else if(RMPsnJobVO.PK_REG_DEPT.equals(evt.getKey())){
				//billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_JOB);
				UIRefPane refPane = (UIRefPane) billModel.getItemByKey(RMPsnJobVO.PK_REG_DEPT).getComponent();
				billModel.setValueAt(null, row, RMPsnJobVO.PK_REG_ORG);
				billModel.setValueAt((String)refPane.getRefValue("pk_father"), row, RMPsnJobVO.PK_REG_ORG);
			}
			// �޸���ӦƸְλ, ������Ƹְλ��ӦƸ��֯��ӦƸ����
			else if(RMPsnJobVO.PK_REG_JOB.equals(evt.getKey()) && evt.getValue()!=null){
				UIRefPane refPane = (UIRefPane) getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName()).getItemByKey(RMPsnJobVO.PK_REG_JOB).getComponent();
				RMPsnJobVO jobVO = (RMPsnJobVO) billModel.getBodyValueRowVO(row, RMPsnJobVO.class.getName());
				jobVO.setPk_reg_org((String) refPane.getRefValue(PublishJobVO.PK_RMORG));
				jobVO.setPk_reg_dept((String) refPane.getRefValue(PublishJobVO.PK_RMDEPT));
				jobVO.setPk_jobsource((String) refPane.getRefValue(PublishJobVO.PK_JOB));
				jobVO.setPk_active((String)refPane.getRefValue(PublishJobVO.PK_ACTIVITY));
				jobVO.setPk_channel((String)refPane.getRefValue(PublishJobVO.PK_CHANNEL));
				billModel.setBodyRowObjectByMetaData(jobVO, row);
				// ͬ����������
				//synPsnCapaTable();
				synPsnCapaInfo();
			}
		}
	}
	public void synPsnCapaInfo(){
		//�ռ�ӦƸְλҳǩ��ְλ��������jobList
		BillModel jobBillModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
		int jobSize = jobBillModel.getRowCount();
		List<String> jobList =  new ArrayList<String>();
		for(int i = 0;i < jobSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnJobVO.PK_REG_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || jobList.contains(pk_publishjob))
				continue;
			jobList.add(pk_publishjob);//ӦƸְλҳǩ��ְλ������������
		}
		//�ռ���������ҳǩ��ְλ��������cpList
		BillModel cpBillModel = getBillCardPanel().getBillModel(RMPsnCPVO.getDefaultTableName());
		int cpSize = cpBillModel.getRowCount();
		List<String> cpList =  new ArrayList<String>();
		for(int i = 0;i < cpSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) cpBillModel.getValueObjectAt(i, RMPsnCPVO.PK_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
//			if(StringUtils.isEmpty(pk_publishjob) || cpList.contains(pk_publishjob))
//				continue;
			cpList.add(pk_publishjob);//ӦƸְλҳǩ��ְλ������������
		}
		//��Ҫ����cp��publish����
		List<String> addList = new ArrayList<String>();
		List<Integer> delList = new ArrayList<Integer>();
		//����Ҫ����cp��publish����
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
		// ɾ��ָ��
		if(!CollectionUtils.isEmpty(delList))
			cpBillModel.delLine(ArrayUtils.toPrimitive(delList.toArray(new Integer[0])));
		// ����ָ��
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
		// ����ָ���в���ֵ
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
	 * ӦƸְλ�仯��ͬ�����������Ӽ�
	 */
	public void synPsnCapaTable(){
		//�ռ�ӦƸְλҳǩ��ְλ��������jobList
		//�ռ���������ҳǩ��ְλ��������cpList
		//Ƕ��ѭ��jobList,cpList ��¼
		BillModel jobBillModel = getBillCardPanel().getBillModel(RMPsnJobVO.getDefaultTableName());
		int jobSize = jobBillModel.getRowCount();
		// ȡ��ǰ�����ϵ�����ӦƸְλ��������Ҫȥ��
		List<String> allJobs = new ArrayList<String>();
		for(int i = 0;i < jobSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnJobVO.PK_REG_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || allJobs.contains(pk_publishjob))
				continue;
			allJobs.add(pk_publishjob);//ӦƸְλҳǩ��ְλ������������
		}
		BillModel cpBillModel = getBillCardPanel().getBillModel(RMPsnCPVO.getDefaultTableName());
		int cpSize = cpBillModel.getRowCount();
		// ��ɾ������
		List<Integer> delRowList = new ArrayList<Integer>();
		// ����ְλ����
		List<String> existJobs = new ArrayList<String>();
		// ѭ������ָ���Ӽ���������ɺ�delRowList��ΪҪɾ����ָ���У�allJobs��ΪҪ�����ݿ��в�ѯ�����ŵ�ָ������ϵ�ְλ
		for(int i = 0;i < cpSize;i++){
			DefaultConstEnum obj = (DefaultConstEnum) jobBillModel.getValueObjectAt(i, RMPsnCPVO.PK_JOB);
			String pk_publishjob = obj==null?null:(String)obj.getValue();
			if(StringUtils.isEmpty(pk_publishjob) || !(allJobs.contains(pk_publishjob) || existJobs.contains(pk_publishjob))){
				delRowList.add(i);
				continue;
			}
			// �������ְλ�������Ѵ������ٴ���
			if(existJobs.contains(pk_publishjob))
				continue;
			// ����Ҫ������ְλ������ɾ�������������뵽����ְλ������
			allJobs.remove(pk_publishjob);
			existJobs.add(pk_publishjob);
		}
		// ɾ��ָ��
		if(!CollectionUtils.isEmpty(delRowList))
			cpBillModel.delLine(ArrayUtils.toPrimitive(delRowList.toArray(new Integer[0])));
		// ����ָ��
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
		// ����ָ���в���ֵ
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
		// ����ӦƸְλ��
		AggRMPsndocVO showVO = (AggRMPsndocVO) billCardPanel.getBillData().getBillObjectByMetaData();	//ҳ�濴�������ݣ�������ɾ�������ݣ�
		aggVO.getPsndocVO().setJobsize(ArrayUtils.getLength(showVO.getTableVO(RMPsnJobVO.getDefaultTableName())));
		return aggVO;
	}

	public void setComponentVisible(boolean visible){
		// ����ʾ״̬δ�ı�ʱ������������ʾ״̬������ᴥ���б�Ƭҳǩͬ������
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
