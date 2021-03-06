package de.gravitex.bpm.traindepartment.logic;

public class DepartTrainProcessConstants {
	
	// ##############################################################################
	// ####################################### links
	// ##############################################################################
	
	public static final String CATCH_LINK_ALL_PROMPTED_WAGGON_MASTER = "CatchLinkAllPrompted";
	public static final String THROW_LINK_ALL_PROMPTED_WAGGON_MASTER = "ThrowLinkAllPrompted";
	
	// ##############################################################################
	// ####################################### roles
	// ##############################################################################
	
	public static final String ROLE_WAGGON_MASTER = "ROLE_WAGGON_MASTER";
	public static final String ROLE_REPAIR_DUDE = "ROLE_REPAIR_DUDE";
	public static final String ROLE_SHUNTER = "ROLE_SHUNTER";
	public static final String ROLE_DISPONENT = "ROLE_DISPONENT";
	public static final String ROLE_SUPERVISOR = "ROLE_SUPERVISOR";

	// ##############################################################################
	// ####################################### tasks
	// ##############################################################################
	
	public static final String TASK_CHOOSE_EXIT_TRACK = "TASK_CHOOSE_EXIT_TRACK";
	public static final String TASK_CHECK_WAGGONS = "TASK_CHECK_WAGGONS";
	public static final String TASK_CONFIRM_ROLLOUT = "TASK_CONFIRM_ROLLOUT";
	public static final String TASK_ASSUME_REPAIR_TIME = "TASK_ASSUME_REPAIR_TIME";
	public static final String TASK_SHUNT_WAGGONS = "TASK_SHUNT_WAGGONS";
	public static final String TASK_REPAIR_WAGGON = "TASK_REPAIR_WAGGON";
	public static final String TASK_EVALUATE_WAGGON = "TASK_EVALUATE_WAGGON";
	public static final String TASK_PROMPT_WAGGON_REPLACEMENT = "TASK_PROMPT_WAGGON_REPLACEMENT";
	public static final String TASK_PROMPT_WAGGON_REPAIR = "TASK_PROMPT_WAGGON_REPAIR";
	public static final String TASK_CHOOSE_REPLACEMENT_TRACK = "TASK_CHOOSE_REPLACEMENT_TRACK";	
	public static final String TASK_CHECK_WAGGON_RUNNABILITY = "TASK_CHECK_WAGGON_RUNNABILITY";
	public static final String TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT = "TASK_PROMPT_REPAIR_WAGGON_REPLACEMENT";

	// ##############################################################################
	// ####################################### signals
	// ##############################################################################
	
	public static final String SIG_RO_CANC = "SIG_RO_CANC";
	
	// ##############################################################################
	// ####################################### collection variables (multi instances, how to
	// ####################################### replace by call to 'WaggonList' process object? --> works!!)
	// ##############################################################################
	
	public static final String VAR_PLANNED_WAGGON = "VAR_PLANNED_WAGGON";
	
	// ##############################################################################
	// ####################################### variables
	// ##############################################################################
	
	// 'Backbone' - Objekt für den gesamten Prozess
	public static final String VAR_DEPARTMENT_PROCESS_DATA = "VAR_DEPARTMENT_PROCESS_DATA";
	
	// planned departure time
	public static final String VAR_PLANNED_DEPARTMENT_DATE = "VAR_PLANNED_DEPARTMENT_DATE";
	
	public static final String VAR_ROLLOUT_CONFIRMED = "VAR_ROLLOUT_CONFIRMED";
	
	public static final String VAR_EXIT_TRACK = "VAR_EXIT_TRACK";
	
	// a single assumed waggon
	public static final String VAR_ASSUMED_WAGGON = "VAR_ASSUMED_WAGGON";
	
	// Wird durch den Reparatur-Prozess geschleift und auuch von diesem zurückgegeben
	public static final String VAR_SINGLE_FACILITY_PROCESS_WAGGON = "VAR_SINGLE_FACILITY_PROCESS_WAGGON";
	
	// Die für einen Wagen abgeschätzte Reparatur-Zeit
	public static final String VAR_ASSUMED_TIME = "VAR_ASSUMED_TIME";
	
	// Aufsummierte Zeitabschätzungen
	public static final String VAR_SUMMED_UP_ASSUMED_HOURS = "VAR_SUMMED_UP_ASSUMED_HOURS";
	
	// an evaluation result for a single waggon in the evaluation sub process
	public static final String VAR_WAGGON_EVALUATION_RESULT = "VAR_WAGGON_EVALUATION_RESULT";
	
	// single waggon to prompt a repair... 
	public static final String VAR_PROMPT_REPAIR_WAGGON = "VAR_PROMPT_REPAIR_WAGGON";

	// single waggon to prompt a replacement... 
	public static final String VAR_PROMPT_REPLACE_WAGGON = "VAR_PROMPT_REPLACE_WAGGON";

	// target track for replaced waggons
	public static final String VAR_REPLACE_WAGGON_TARGET_TRACK = "VAR_REPLACE_WAGGON_TARGET_TRACK";
	
	// Die Wagen, die als Ersatz geliefert wurden
	public static final String VAR_DELIVERED_REPLACMENT_WAGGONS = "VAR_DELIVERED_REPLACMENT_WAGGONS";
	
	public static final String VAR_SINGLE_WAGGON_RUNNABLE = "VAR_SINGLE_WAGGON_RUNNABLE";
	
	// Ein Wagen, der vom Facility-Prozess als repariert zurückgemeldet wurde
	public static final String VAR_REPAIRED_WAGGON = "VAR_REPAIRED_WAGGON";
	
	// Ein Wagen, der vom Facility-Prozess als 'repair tiemout' zurückgemeldet wurde
	public static final String VAR_WAGGON_REPAIR_TIMEOUT = "VAR_WAGGON_REPAIR_TIMEOUT";
	
	// business key of the 'master' process --> passed to repair 
	// process to able to call back to master
	public static final String VAR_DEP_PROC_BK = "VAR_DEP_PROC_BK";
	
	// Liste mit allen Wagen, die vom Facility-Prozess als 'repair exceeded' zurückgemeldet wurden...
	public static final String VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST = "VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST";
	
	// Ein einzelner Wagen aus 'VAR_WAGGONS_REPAIR_TIME_EXCEEDED_LIST' (s.o.)
	public static final String VAR_WAGGON_REPAIR_TIME_EXCEEDED = "VAR_WAGGON_REPAIR_TIME_EXCEEDED";
	
	// assumed reapair deadline base on waggon repair assumption
	public static final String VAR_TIMER_EXCEEDED_REPAIR_TIME = "VAR_TIMER_EXCEEDED_REPAIR_TIME";
	
	// ##############################################################################
	// ####################################### messages
	// ##############################################################################
	
	public static final String MSG_DEPARTURE_PLANNED = "MSG_DEPARTURE_PLANNED";
	public static final String MSG_REPAIR_ASSUMED = "MSG_REPAIR_ASSUMED";
	public static final String MSG_SH_ORD = "MSG_SH_ORD";
	public static final String MSG_INVOKE_WAG_ASSUMEMENT = "MSG_INVOKE_WAG_ASSUMEMENT";
	public static final String MSG_START_REPAIR = "MSG_START_REPAIR";
	public static final String MSG_ABORT_REPAIR = "MSG_ABORT_REPAIR";
	public static final String MSG_REPL_WAGG_ARRIVED = "MSG_REPL_WAGG_ARRIVED";
	public static final String MSG_REPAIR_DONE = "MSG_REPAIR_DONE";
	public static final String MSG_REPAIR_TIME_EXCEEDED = "MSG_REPAIR_TIME_EXCEEDED";
	public static final String MSG_REP_REPLACE_ARR = "MSG_REP_REPLACE_ARR";
	public static final String MSG_SH_DONE = "MSG_SH_DONE";
	
	// ##############################################################################
	// ####################################### errors
	// ##############################################################################
	
	public static final String ERR_NO_EXIT_TR = "ERR_NO_EXIT_TR";
	public static final String ERR_CREATE_DO = "ERR_CREATE_DO";
	public static final String ERR_WG_NOT_RUNNABLE = "ERR_WG_NOT_RUNNABLE";
	
	// ##############################################################################
	// ####################################### message catchers
	// ##############################################################################
	
	public static final String CATCH_MSG_SH_DONE = "CATCH_MSG_SH_DONE";
	public static final String CATCH_MSG_START_REPAIR = "CATCH_MSG_START_REPAIR";
	public static final String CATCH_MSG_REP_WAGG_ARRIVED = "CATCH_MSG_REP_WAGG_ARRIVED";
	public static final String CATCH_MSG_WG_ASSUMED = "CATCH_MSG_WG_ASSUMED";
	public static final String CATCH_MSG_REP_REPLACE_ARR = "CATCH_MSG_REP_REPLACE_ARR";
	public static final String CATCH_MSG_REPAIR_TIME_EXCEEDED = "CATCH_MSG_REPAIR_TIME_EXCEEDED";
	
	// ##############################################################################
	// ####################################### elements
	// ##############################################################################
	
	// gateways (exclusive)
	
	public static final String GW_REPAIR_CALLBACK = "MsGwRepairCallback";
	
	// gateways (parallel)
	
	public static final String GW_PLACE_REPLACEMENT_WAGGONS = "GW_PLACE_REPLACEMENT_WAGGONS";
	
	// gateways (event)
	
	public static final String GW_START_OR_ABORT_REPAIR = "ExGwStartOrAbortRepair";
	public static final String GW_AWAIT_REPAIR_OUTCOME = "ExGwAwaitRepairOutcome";
	
	// ##############################################################################
	// ####################################### process definitions
	// ##############################################################################
	
	public static final String PROCESS_DEPART_TRAIN = "PROCESS_DEPART_TRAIN";
	public static final String PROCESS_REPAIR_FACILITY = "PROCESS_REPAIR_FACILITY";
	public static final String PROCESS_SHUNTER = "PROCESS_SHUNTER";
}