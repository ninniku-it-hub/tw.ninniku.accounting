package tw.ninniku.accounting.process;

import org.compiere.model.MClient;
import org.compiere.model.MProcessPara;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class CopyDefaultProductAccouning extends SvrProcess {

	private int p_AD_Client_ID;
	private int p_C_AcctSchema_ID;

	@Override
	protected void prepare() {
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
				p_AD_Client_ID = para.getParameterAsInt();
			else if (name.equals("C_AcctSchema_ID"))
				p_C_AcctSchema_ID = para.getParameterAsInt();
			else
				MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), para);
		}

	}

	@Override
	protected String doIt() throws Exception {
//		int i_AD_Client_ID = getAD_Client_ID();
//			MClient client = new MClient(getCtx(), i_AD_Client_ID, get_TrxName());			
//		int i_C_AcctSchema_ID =     client.getAcctSchema().getC_AcctSchema_ID();
		
		// update category acctg
		String sql = "update M_Product_Category_Acct pca "
				+ "set p_sales_allowances_acct = (select p_sales_allowances_acct from C_AcctSchema_Default where  C_AcctSchema_ID=pca.C_AcctSchema_ID), "
				+ "p_sales_returns_acct = (select p_sales_returns_acct from C_AcctSchema_Default where  C_AcctSchema_ID=pca.C_AcctSchema_ID), "
				+ "p_purchase_allowances_acct = (select p_purchase_allowances_acct from C_AcctSchema_Default where  C_AcctSchema_ID=pca.C_AcctSchema_ID), "
				+ "p_purchase_returns_acct = (select p_purchase_returns_acct from C_AcctSchema_Default where  C_AcctSchema_ID=pca.C_AcctSchema_ID) "
				+ "where "
				+ "ad_client_id = ?"
				+ "and C_AcctSchema_ID = ?"
				+ "and (p_sales_returns_acct is null "
				+ "or p_sales_allowances_acct is null "
				+ "or p_purchase_returns_acct is null "
				+ "or p_purchase_allowances_acct is null "
				+ ")";
		
		int counter = DB.executeUpdate(sql, new Object[] {p_AD_Client_ID,p_C_AcctSchema_ID}, true, get_TrxName());
		
		// update product acct 
		sql = "update M_Product_acct pca "
				+ "set p_sales_allowances_acct = (select p_sales_allowances_acct from M_Product_Category_Acct where  C_AcctSchema_ID=pca.C_AcctSchema_ID "
				+ "							   and exists (select 1 from m_product mp where mp.m_product_id = pca.m_product_id and M_Product_Category_Acct.m_product_category_id = mp.m_product_category_id)), "
				+ "   p_sales_returns_acct = (select p_sales_returns_acct from M_Product_Category_Acct where  C_AcctSchema_ID=pca.C_AcctSchema_ID "
				+ "							   and exists (select 1 from m_product mp where mp.m_product_id = pca.m_product_id and M_Product_Category_Acct.m_product_category_id = mp.m_product_category_id)), "
				+ "p_purchase_allowances_acct = (select p_purchase_allowances_acct from M_Product_Category_Acct where  C_AcctSchema_ID=pca.C_AcctSchema_ID "
				+ "							   and exists (select 1 from m_product mp where mp.m_product_id = pca.m_product_id and M_Product_Category_Acct.m_product_category_id = mp.m_product_category_id)), "
				+ "p_purchase_returns_acct = (select p_purchase_returns_acct from M_Product_Category_Acct where  C_AcctSchema_ID=pca.C_AcctSchema_ID "
				+ "							   and exists (select 1 from m_product mp where mp.m_product_id = pca.m_product_id and M_Product_Category_Acct.m_product_category_id = mp.m_product_category_id)) "
				+ "where "
				+ "ad_client_id = ?"
				+ "and C_AcctSchema_ID = ?"
				+ "and (p_sales_returns_acct is null "
				+ "or p_sales_allowances_acct is null "
				+ "or p_purchase_returns_acct is null "
				+ "or p_purchase_allowances_acct is null "
				+ ")";
		
		counter += DB.executeUpdate(sql, new Object[] {p_AD_Client_ID,p_C_AcctSchema_ID}, true, get_TrxName());
		return "update " + counter + " recordes";
	}

}
