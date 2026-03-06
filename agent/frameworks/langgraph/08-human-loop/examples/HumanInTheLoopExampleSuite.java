/**
 * LangGraph Human-in-the-Loop Examples - Complete Implementation
 * Demonstrates interactive workflows with human intervention points
 */
package com.example.agent.langgraph.humanloop;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Comprehensive Human-in-the-Loop demonstration
 */
public class HumanInTheLoopExampleSuite {

    private static HumanInTheLoop hitlSystem;

    public static void main(String[] args) {
        System.out.println("=== LangGraph Human-in-the-Loop Examples ===");

        try {
            // Initialize HITL system
            setupHITLSystem();

            // Run comprehensive HITL examples
            runBasicApprovalWorkflow();
            runContentReviewWorkflow();
            runEscalationWorkflow();
            runCollaborativeEditingWorkflow();
            runQualityAssuranceWorkflow();
            runMultiStageApprovalWorkflow();
            runAdvancedHITLPatterns();

        } catch (Exception e) {
            System.err.println("Error running HITL examples: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * Setup HITL system
     */
    private static void setupHITLSystem() throws Exception {
        System.out.println("Setting up Human-in-the-Loop system...");

        hitlSystem = new InMemoryHumanInTheLoop("development_hitl");
        hitlSystem.initialize(Map.of(
            "default_timeout", Duration.ofMinutes(10),
            "max_pending_requests", 100,
            "enable_auto_handlers", true
        ));

        // Register mock handlers for automatic processing
        hitlSystem.registerHandler("approval", new MockApprovalHandler());
        hitlSystem.registerHandler("review", new MockReviewHandler());
        hitlSystem.registerHandler("escalation", new MockEscalationHandler());

        System.out.println("HITL system initialized with mock handlers");
    }

    /**
     * Basic approval workflow
     */
    private static void runBasicApprovalWorkflow() throws Exception {
        System.out.println("\n1. Basic Approval Workflow:");

        // Document publication approval
        Map<String, Object> documentToApprove = Map.of(
            "title", "Company Policy Update",
            "content", "Updated remote work policy for Q2 2024...",
            "author", "HR Department",
            "classification", "safe"  // This will trigger auto-approval in mock handler
        );

        ApprovalRequest approvalRequest = new ApprovalRequest(
            "approval_001",
            "document_publishing_workflow",
            "exec_001",
            "Document Publication Approval",
            "Please review and approve the company policy update for publication",
            documentToApprove,
            ApprovalType.SINGLE_APPROVER,
            List.of("manager", "hr_lead"),
            false,
            Duration.ofMinutes(30),
            Priority.NORMAL
        );

        System.out.println("Requesting approval for document: " + documentToApprove.get("title"));

        CompletableFuture<ApprovalResponse> approvalFuture = hitlSystem.requestApproval(approvalRequest);

        // Wait for approval (mock handler will respond automatically)
        ApprovalResponse approvalResult = approvalFuture.get(5, TimeUnit.SECONDS);

        System.out.println("Approval result:");
        System.out.println("  Approved: " + approvalResult.isApproved());
        System.out.println("  Approvers: " + approvalResult.getApproverIds());
        System.out.println("  Comments: " + approvalResult.getComments());

        // Test rejection scenario
        Map<String, Object> sensitiveDocument = Map.of(
            "title", "Confidential Financial Report",
            "content", "Internal financial data and projections...",
            "classification", "sensitive"  // This will trigger rejection
        );

        ApprovalRequest sensitiveRequest = new ApprovalRequest(
            "approval_002",
            "document_publishing_workflow",
            "exec_002",
            "Sensitive Document Approval",
            "Please review confidential financial report",
            sensitiveDocument,
            ApprovalType.MULTI_APPROVER,
            List.of("cfo", "legal", "security"),
            true, // Requires consensus
            Duration.ofHours(2),
            Priority.HIGH
        );

        CompletableFuture<ApprovalResponse> sensitiveApprovalFuture = hitlSystem.requestApproval(sensitiveRequest);
        ApprovalResponse sensitiveResult = sensitiveApprovalFuture.get(5, TimeUnit.SECONDS);

        System.out.println("\nSensitive document approval result:");
        System.out.println("  Approved: " + sensitiveResult.isApproved());
        System.out.println("  Reason: " + sensitiveResult.getComments());
    }

    /**
     * Content review workflow
     */
    private static void runContentReviewWorkflow() throws Exception {
        System.out.println("\n2. Content Review Workflow:");

        // AI-generated content for review
        String aiGeneratedContent = "Artificial Intelligence is revolutionizing the way businesses operate. " +
                                  "From automated customer service to predictive analytics, AI technologies " +
                                  "are enabling companies to improve efficiency, reduce costs, and enhance " +
                                  "customer experiences. This transformation requires careful consideration " +
                                  "of ethical implications and responsible implementation practices.";

        // Define review criteria
        List<ReviewCriterion> contentCriteria = Arrays.asList(
            new ReviewCriterion("accuracy", "Content factual accuracy", CriterionType.SCALE, true, 8.0),
            new ReviewCriterion("clarity", "Writing clarity and readability", CriterionType.SCALE, true, 7.0),
            new ReviewCriterion("tone", "Appropriate tone for audience", CriterionType.CATEGORICAL, true, "professional"),
            new ReviewCriterion("completeness", "Content completeness", CriterionType.BINARY, true, true),
            new ReviewCriterion("compliance", "Compliance with guidelines", CriterionType.CHECKLIST, false, null)
        );

        ReviewRequest contentReview = new ReviewRequest(
            "review_001",
            "content_generation_workflow",
            "exec_003",
            "AI Content Review",
            "Please review AI-generated content for publication approval",
            aiGeneratedContent,
            ReviewType.CONTENT_REVIEW,
            contentCriteria,
            List.of("content_reviewer", "subject_expert"),
            Duration.ofHours(1),
            Priority.NORMAL
        );

        System.out.println("Submitting content for review:");
        System.out.println("  Content length: " + aiGeneratedContent.length() + " characters");
        System.out.println("  Review criteria: " + contentCriteria.size());

        CompletableFuture<ReviewResponse> reviewFuture = hitlSystem.submitForReview(contentReview);
        ReviewResponse reviewResult = reviewFuture.get(5, TimeUnit.SECONDS);

        System.out.println("Review completed:");
        System.out.println("  Passed: " + reviewResult.isPassed());
        System.out.println("  Overall score: " + String.format("%.2f", reviewResult.getOverallScore()));
        System.out.println("  Issues: " + reviewResult.getIssues());
        System.out.println("  Recommendations: " + reviewResult.getRecommendations());
        System.out.println("  Criterion results:");

        reviewResult.getCriterionResults().forEach((criterion, result) ->
            System.out.println("    " + criterion + ": " + result));

        // Technical review example
        Map<String, Object> codeToReview = Map.of(
            "language", "Java",
            "lines_of_code", 150,
            "complexity_score", 6.5,
            "test_coverage", 85.0,
            "security_scan_passed", true
        );

        List<ReviewCriterion> technicalCriteria = Arrays.asList(
            new ReviewCriterion("code_quality", "Code quality and style", CriterionType.SCALE, true, 8.0),
            new ReviewCriterion("performance", "Performance considerations", CriterionType.SCALE, true, 7.0),
            new ReviewCriterion("security", "Security best practices", CriterionType.BINARY, true, true),
            new ReviewCriterion("documentation", "Code documentation", CriterionType.SCALE, false, 6.0)
        );

        ReviewRequest techReview = new ReviewRequest(
            "review_002",
            "code_deployment_workflow",
            "exec_004",
            "Code Review",
            "Technical review of code changes before deployment",
            codeToReview,
            ReviewType.TECHNICAL_REVIEW,
            technicalCriteria,
            List.of("senior_developer", "architect"),
            Duration.ofHours(4),
            Priority.HIGH
        );

        CompletableFuture<ReviewResponse> techReviewFuture = hitlSystem.submitForReview(techReview);
        ReviewResponse techReviewResult = techReviewFuture.get(5, TimeUnit.SECONDS);

        System.out.println("\nTechnical review completed:");
        System.out.println("  Code review passed: " + techReviewResult.isPassed());
        System.out.println("  Technical score: " + String.format("%.2f", techReviewResult.getOverallScore()));
        System.out.println("  Reviewer comments: " + techReviewResult.getComments());
    }

    /**
     * Escalation workflow
     */
    private static void runEscalationWorkflow() throws Exception {
        System.out.println("\n3. Escalation Workflow:");

        // System error escalation
        Map<String, Object> systemErrorContext = Map.of(
            "error_code", "DB_CONNECTION_TIMEOUT",
            "error_message", "Database connection timeout after 30 seconds",
            "affected_users", 150,
            "service_impact", "HIGH",
            "attempted_solutions", Arrays.asList("Connection pool restart", "Database ping test", "Failover attempt"),
            "system_logs", "2024-03-05 14:30:15 ERROR DatabaseManager: Connection timeout..."
        );

        EscalationRequest systemEscalation = new EscalationRequest(
            "escalation_001",
            "user_service_workflow",
            "exec_005",
            null, // No original request
            EscalationType.SYSTEM_ERROR,
            "Database connection timeout affecting user service",
            systemErrorContext,
            List.of("sre_engineer", "database_admin", "incident_commander"),
            Priority.CRITICAL
        );

        System.out.println("Escalating system error:");
        System.out.println("  Error: " + systemErrorContext.get("error_message"));
        System.out.println("  Impact: " + systemErrorContext.get("service_impact"));
        System.out.println("  Affected users: " + systemErrorContext.get("affected_users"));

        CompletableFuture<EscalationResponse> systemEscalationFuture = hitlSystem.escalateToHuman(systemEscalation);
        EscalationResponse systemResolution = systemEscalationFuture.get(5, TimeUnit.SECONDS);

        System.out.println("System escalation resolved:");
        System.out.println("  Resolution: " + systemResolution.getResolution());
        System.out.println("  Action: " + systemResolution.getAction());
        System.out.println("  Follow-up required: " + systemResolution.isRequiresFollowUp());
        System.out.println("  Resolver: " + systemResolution.getResponderId());

        // Business logic escalation
        Map<String, Object> businessContext = Map.of(
            "customer_id", "CUST_12345",
            "order_amount", 25000.00,
            "discount_requested", 15.0,
            "customer_tier", "gold",
            "previous_orders", 47,
            "account_standing", "good",
            "reason", "Bulk purchase discount request exceeds standard policy"
        );

        EscalationRequest businessEscalation = new EscalationRequest(
            "escalation_002",
            "order_processing_workflow",
            "exec_006",
            "approval_003", // Related to failed approval
            EscalationType.BUSINESS_LOGIC,
            "Discount request exceeds policy limits",
            businessContext,
            List.of("sales_manager", "finance_director"),
            Priority.HIGH
        );

        CompletableFuture<EscalationResponse> businessEscalationFuture = hitlSystem.escalateToHuman(businessEscalation);
        EscalationResponse businessResolution = businessEscalationFuture.get(5, TimeUnit.SECONDS);

        System.out.println("\nBusiness escalation resolved:");
        System.out.println("  Customer: " + businessContext.get("customer_id"));
        System.out.println("  Resolution: " + businessResolution.getResolution());
        System.out.println("  Action taken: " + businessResolution.getAction());

        // Data quality escalation
        Map<String, Object> dataQualityContext = Map.of(
            "dataset", "customer_addresses",
            "quality_issues", Arrays.asList("Missing postal codes", "Invalid email formats", "Duplicate records"),
            "affected_records", 1247,
            "total_records", 15000,
            "data_source", "CRM import",
            "quality_score", 0.67
        );

        EscalationRequest dataEscalation = new EscalationRequest(
            "escalation_003",
            "data_processing_workflow",
            "exec_007",
            null,
            EscalationType.DATA_QUALITY,
            "Data quality below acceptable threshold",
            dataQualityContext,
            List.of("data_steward", "quality_analyst"),
            Priority.NORMAL
        );

        CompletableFuture<EscalationResponse> dataEscalationFuture = hitlSystem.escalateToHuman(dataEscalation);
        EscalationResponse dataResolution = dataEscalationFuture.get(5, TimeUnit.SECONDS);

        System.out.println("\nData quality escalation resolved:");
        System.out.println("  Dataset: " + dataQualityContext.get("dataset"));
        System.out.println("  Quality score: " + dataQualityContext.get("quality_score"));
        System.out.println("  Resolution: " + dataResolution.getResolution());
    }

    /**
     * Collaborative editing workflow
     */
    private static void runCollaborativeEditingWorkflow() throws Exception {
        System.out.println("\n4. Collaborative Editing Workflow:");

        // AI-generated document that needs human refinement
        String initialDraft = "Executive Summary: Q1 2024 Performance Report\\n\\n" +
                            "Our company achieved significant milestones in Q1 2024. Revenue increased by 15% " +
                            "compared to Q1 2023, reaching $2.3 million. Key performance indicators showed " +
                            "positive trends across all departments. Customer satisfaction scores improved " +
                            "to 4.2/5.0. The marketing team successfully launched 3 new campaigns.";

        CollaborativeEditingWorkflow collaborativeWorkflow = new CollaborativeEditingWorkflow(hitlSystem);

        // Stage 1: Initial review and feedback
        String docId = collaborativeWorkflow.startCollaboration(
            "quarterly_report_workflow",
            "exec_008",
            initialDraft,
            List.of("content_editor", "financial_analyst", "marketing_manager")
        );

        System.out.println("Started collaborative editing:");
        System.out.println("  Document ID: " + docId);
        System.out.println("  Initial draft length: " + initialDraft.length() + " characters");

        // Simulate collaborative feedback
        Map<String, String> feedback = Map.of(
            "content_editor", "Good structure, but needs more specific metrics in the marketing section",
            "financial_analyst", "Revenue figure is correct, suggest adding profit margin data",
            "marketing_manager", "Campaign details need expansion - include ROI figures"
        );

        String revisedDraft = collaborativeWorkflow.incorporateFeedback(docId, feedback);
        System.out.println("Incorporated feedback, revised draft length: " + revisedDraft.length() + " characters");

        // Stage 2: Final review and approval
        boolean finalApproval = collaborativeWorkflow.submitForFinalApproval(docId,
            List.of("ceo", "cfo"), Duration.ofHours(2));

        System.out.println("Final approval result: " + (finalApproval ? "Approved" : "Rejected"));

        // Document versioning
        List<String> versionHistory = collaborativeWorkflow.getVersionHistory(docId);
        System.out.println("Document versions: " + versionHistory);
    }

    /**
     * Quality assurance workflow
     */
    private static void runQualityAssuranceWorkflow() throws Exception {
        System.out.println("\n5. Quality Assurance Workflow:");

        QualityAssuranceWorkflow qaWorkflow = new QualityAssuranceWorkflow(hitlSystem);

        // Product quality check
        Map<String, Object> productData = Map.of(
            "product_id", "PRD_2024_001",
            "product_name", "Smart Home Hub",
            "version", "2.1.0",
            "test_results", Map.of(
                "functionality_tests", 98,
                "performance_tests", 95,
                "security_tests", 97,
                "usability_tests", 92
            ),
            "defect_count", 3,
            "critical_defects", 0
        );

        // Define QA criteria
        List<QualityCriterion> qaCriteria = Arrays.asList(
            new QualityCriterion("functionality", "All features work correctly", 95.0),
            new QualityCriterion("performance", "Meets performance benchmarks", 90.0),
            new QualityCriterion("security", "Security standards compliance", 98.0),
            new QualityCriterion("usability", "User experience quality", 85.0),
            new QualityCriterion("reliability", "System stability", 92.0)
        );

        String qaRequestId = qaWorkflow.initiateQualityCheck(
            "product_release_workflow",
            "exec_009",
            productData,
            qaCriteria,
            List.of("qa_lead", "product_manager")
        );

        System.out.println("Quality assurance check initiated:");
        System.out.println("  Product: " + productData.get("product_name"));
        System.out.println("  Version: " + productData.get("version"));
        System.out.println("  QA Request ID: " + qaRequestId);

        // Wait for QA completion
        QualityAssessmentResult qaResult = qaWorkflow.getQualityAssessment(qaRequestId, Duration.ofMinutes(1));

        System.out.println("QA Assessment completed:");
        System.out.println("  Overall quality passed: " + qaResult.isPassed());
        System.out.println("  Quality score: " + String.format("%.1f%%", qaResult.getOverallScore() * 100));
        System.out.println("  Critical issues: " + qaResult.getCriticalIssues().size());
        System.out.println("  Recommendations: " + qaResult.getRecommendations().size());

        if (!qaResult.isPassed()) {
            System.out.println("  Failed criteria:");
            qaResult.getFailedCriteria().forEach(criterion ->
                System.out.println("    - " + criterion));
        }
    }

    /**
     * Multi-stage approval workflow
     */
    private static void runMultiStageApprovalWorkflow() throws Exception {
        System.out.println("\n6. Multi-Stage Approval Workflow:");

        MultiStageApprovalWorkflow approvalWorkflow = new MultiStageApprovalWorkflow(hitlSystem);

        // Budget request that needs multi-level approval
        Map<String, Object> budgetRequest = Map.of(
            "request_id", "BUD_2024_Q2_001",
            "department", "Engineering",
            "requested_amount", 150000.0,
            "purpose", "Cloud infrastructure upgrade",
            "justification", "Improve system performance and scalability for expected 50% user growth",
            "timeline", "Q2 2024",
            "roi_estimate", "15% cost reduction in Q3-Q4"
        );

        // Define approval stages
        List<ApprovalStage> approvalStages = Arrays.asList(
            new ApprovalStage("department_manager", List.of("eng_manager"), 50000.0),
            new ApprovalStage("finance_review", List.of("finance_director"), 100000.0),
            new ApprovalStage("executive_approval", List.of("ceo", "cfo"), Double.MAX_VALUE)
        );

        String approvalChainId = approvalWorkflow.startApprovalChain(
            "budget_approval_workflow",
            "exec_010",
            budgetRequest,
            approvalStages
        );

        System.out.println("Multi-stage approval started:");
        System.out.println("  Budget request: $" + String.format("%.0f", budgetRequest.get("requested_amount")));
        System.out.println("  Department: " + budgetRequest.get("department"));
        System.out.println("  Approval chain ID: " + approvalChainId);
        System.out.println("  Approval stages: " + approvalStages.size());

        // Process through approval stages
        for (int stage = 0; stage < approvalStages.size(); stage++) {
            ApprovalStage currentStage = approvalStages.get(stage);
            System.out.println("\n  Processing stage " + (stage + 1) + ": " + currentStage.getStageName());

            ApprovalStageResult stageResult = approvalWorkflow.processApprovalStage(
                approvalChainId, stage, Duration.ofMinutes(1));

            System.out.println("    Stage result: " + stageResult.getStatus());
            System.out.println("    Approver: " + stageResult.getApprover());
            System.out.println("    Comments: " + stageResult.getComments());

            if (stageResult.getStatus() == ApprovalStageStatus.REJECTED) {
                System.out.println("    Approval chain stopped due to rejection");
                break;
            }
        }

        // Get final approval status
        ApprovalChainResult finalResult = approvalWorkflow.getFinalApprovalStatus(approvalChainId);
        System.out.println("\nFinal approval status: " + finalResult.getOverallStatus());
        System.out.println("Completed stages: " + finalResult.getCompletedStages().size());
        System.out.println("Total processing time: " + finalResult.getTotalProcessingTime().toMillis() + "ms");
    }

    /**
     * Advanced HITL patterns
     */
    private static void runAdvancedHITLPatterns() throws Exception {
        System.out.println("\n7. Advanced HITL Patterns:");

        // Pattern 1: Adaptive automation (progressive reduction of human involvement)
        System.out.println("Pattern 1: Adaptive Automation");

        AdaptiveAutomationManager adaptiveManager = new AdaptiveAutomationManager(hitlSystem);

        // Simulate processing similar tasks over time
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> taskData = Map.of(
                "task_id", "TASK_" + String.format("%03d", i),
                "type", "document_classification",
                "confidence_score", 0.7 + (i * 0.05), // Increasing confidence
                "complexity", "medium",
                "historical_accuracy", 0.85 + (i * 0.02)
            );

            boolean requiresHuman = adaptiveManager.shouldRequireHumanReview(taskData);
            System.out.println("  Task " + i + " - Confidence: " + taskData.get("confidence_score") +
                             ", Human review required: " + requiresHuman);

            if (requiresHuman) {
                // Simulate human review and feedback
                adaptiveManager.recordHumanFeedback("TASK_" + String.format("%03d", i), true, 0.95);
            } else {
                // Simulate automated processing
                adaptiveManager.recordAutomatedResult("TASK_" + String.format("%03d", i), 0.92);
            }
        }

        System.out.println("  Automation rate improved: " +
                         String.format("%.1f%%", adaptiveManager.getCurrentAutomationRate() * 100));

        // Pattern 2: Human expertise routing
        System.out.println("\nPattern 2: Human Expertise Routing");

        ExpertiseRouter expertiseRouter = new ExpertiseRouter(hitlSystem);

        // Register experts with their specializations
        expertiseRouter.registerExpert("alice", Arrays.asList("machine_learning", "data_science"));
        expertiseRouter.registerExpert("bob", Arrays.asList("security", "compliance"));
        expertiseRouter.registerExpert("carol", Arrays.asList("user_experience", "design"));
        expertiseRouter.registerExpert("david", Arrays.asList("finance", "business_analysis"));

        // Route different types of requests to appropriate experts
        Map<String, String[]> requestTypes = Map.of(
            "ML model review", new String[]{"machine_learning"},
            "Security audit", new String[]{"security", "compliance"},
            "UI/UX feedback", new String[]{"user_experience", "design"},
            "Cost-benefit analysis", new String[]{"finance", "business_analysis"}
        );

        for (Map.Entry<String, String[]> request : requestTypes.entrySet()) {
            String requestType = request.getKey();
            String[] requiredSkills = request.getValue();

            List<String> matchedExperts = expertiseRouter.findExperts(Arrays.asList(requiredSkills));
            System.out.println("  " + requestType + " -> Experts: " + matchedExperts);

            if (!matchedExperts.isEmpty()) {
                String selectedExpert = expertiseRouter.selectBestExpert(matchedExperts, requestType);
                System.out.println("    Selected: " + selectedExpert);
            }
        }

        // Pattern 3: Escalation chain with fallbacks
        System.out.println("\nPattern 3: Escalation Chain with Fallbacks");

        EscalationChainManager escalationManager = new EscalationChainManager(hitlSystem);

        // Define escalation hierarchy
        List<EscalationLevel> escalationChain = Arrays.asList(
            new EscalationLevel("team_lead", Duration.ofMinutes(15), Arrays.asList("john", "jane")),
            new EscalationLevel("department_manager", Duration.ofMinutes(30), Arrays.asList("mike", "sarah")),
            new EscalationLevel("director", Duration.ofHours(2), Arrays.asList("robert", "lisa")),
            new EscalationLevel("vp", Duration.ofHours(4), Arrays.asList("alex", "maria"))
        );

        // Simulate escalation scenario
        String escalationId = escalationManager.initiateEscalation(
            "critical_system_outage",
            "production_workflow",
            "exec_011",
            "Database cluster failure affecting all services",
            escalationChain
        );

        System.out.println("  Initiated escalation: " + escalationId);

        // Simulate escalation progression
        for (int level = 0; level < escalationChain.size(); level++) {
            EscalationLevel currentLevel = escalationChain.get(level);
            System.out.println("    Escalating to " + currentLevel.getLevelName() +
                             " (timeout: " + currentLevel.getTimeout().toMinutes() + " min)");

            boolean resolved = escalationManager.checkEscalationLevel(escalationId, level);
            if (resolved) {
                System.out.println("    Resolved at " + currentLevel.getLevelName());
                break;
            } else if (level < escalationChain.size() - 1) {
                System.out.println("    No response, escalating to next level");
            } else {
                System.out.println("    Maximum escalation level reached");
            }
        }

        // Pattern 4: Consensus building
        System.out.println("\nPattern 4: Consensus Building");

        ConsensusManager consensusManager = new ConsensusManager(hitlSystem);

        // Decision requiring consensus
        Map<String, Object> decisionContext = Map.of(
            "decision_type", "technology_selection",
            "options", Arrays.asList("Option A: Cloud Provider X", "Option B: Cloud Provider Y", "Option C: Hybrid Solution"),
            "criteria", Arrays.asList("cost", "performance", "security", "vendor_lock_in"),
            "stakeholders", Arrays.asList("architect", "security_lead", "finance_director", "product_manager")
        );

        String consensusId = consensusManager.startConsensusBuilding(
            "technology_selection_workflow",
            "exec_012",
            decisionContext,
            Duration.ofHours(1)
        );

        System.out.println("  Started consensus building: " + consensusId);
        System.out.println("  Decision options: " + decisionContext.get("options"));
        System.out.println("  Stakeholders: " + decisionContext.get("stakeholders"));

        // Simulate consensus building process
        ConsensusResult consensusResult = consensusManager.buildConsensus(consensusId, Duration.ofMinutes(2));

        System.out.println("  Consensus result:");
        System.out.println("    Achieved: " + consensusResult.isConsensusAchieved());
        System.out.println("    Selected option: " + consensusResult.getSelectedOption());
        System.out.println("    Agreement level: " + String.format("%.1f%%", consensusResult.getAgreementLevel() * 100));
        System.out.println("    Dissenting opinions: " + consensusResult.getDissentingOpinions().size());
    }

    /**
     * Cleanup resources
     */
    private static void cleanup() {
        try {
            if (hitlSystem != null) {
                hitlSystem.close();
                System.out.println("HITL system closed");
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}

/**
 * Supporting classes for advanced HITL patterns
 */

class CollaborativeEditingWorkflow {
    private final HumanInTheLoop hitlSystem;
    private final Map<String, DocumentSession> activeSessions = new ConcurrentHashMap<>();

    public CollaborativeEditingWorkflow(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public String startCollaboration(String workflowId, String executionId, String initialContent, List<String> collaborators) {
        String docId = "doc_" + System.currentTimeMillis();
        DocumentSession session = new DocumentSession(docId, workflowId, executionId, initialContent, collaborators);
        activeSessions.put(docId, session);
        return docId;
    }

    public String incorporateFeedback(String docId, Map<String, String> feedback) {
        DocumentSession session = activeSessions.get(docId);
        if (session != null) {
            return session.incorporateFeedback(feedback);
        }
        return "";
    }

    public boolean submitForFinalApproval(String docId, List<String> finalApprovers, Duration timeout) {
        DocumentSession session = activeSessions.get(docId);
        if (session != null) {
            // Simulate final approval
            return Math.random() > 0.2; // 80% approval rate
        }
        return false;
    }

    public List<String> getVersionHistory(String docId) {
        DocumentSession session = activeSessions.get(docId);
        return session != null ? session.getVersionHistory() : List.of();
    }

    private static class DocumentSession {
        private final String docId;
        private final String workflowId;
        private final String executionId;
        private String currentContent;
        private final List<String> collaborators;
        private final List<String> versionHistory = new ArrayList<>();

        public DocumentSession(String docId, String workflowId, String executionId, String initialContent, List<String> collaborators) {
            this.docId = docId;
            this.workflowId = workflowId;
            this.executionId = executionId;
            this.currentContent = initialContent;
            this.collaborators = new ArrayList<>(collaborators);
            this.versionHistory.add("v1.0 - Initial draft");
        }

        public String incorporateFeedback(Map<String, String> feedback) {
            // Simulate content revision based on feedback
            StringBuilder revisedContent = new StringBuilder(currentContent);

            if (feedback.containsKey("financial_analyst")) {
                revisedContent.append("\\n\\nFinancial Details:\\n- Profit margin: 18.5%\\n- Operating costs: $1.95M");
            }

            if (feedback.containsKey("marketing_manager")) {
                revisedContent.append("\\n\\nMarketing Campaign Results:\\n- Campaign 1 ROI: 245%\\n- Campaign 2 ROI: 312%\\n- Campaign 3 ROI: 198%");
            }

            currentContent = revisedContent.toString();
            versionHistory.add("v" + (versionHistory.size() + 1) + ".0 - Incorporated feedback from " + feedback.keySet());

            return currentContent;
        }

        public List<String> getVersionHistory() {
            return new ArrayList<>(versionHistory);
        }
    }
}

class QualityAssuranceWorkflow {
    private final HumanInTheLoop hitlSystem;
    private final Map<String, QualityAssessmentSession> activeSessions = new ConcurrentHashMap<>();

    public QualityAssuranceWorkflow(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public String initiateQualityCheck(String workflowId, String executionId, Map<String, Object> productData,
                                     List<QualityCriterion> criteria, List<String> reviewers) {
        String qaRequestId = "qa_" + System.currentTimeMillis();
        QualityAssessmentSession session = new QualityAssessmentSession(qaRequestId, workflowId, executionId, productData, criteria, reviewers);
        activeSessions.put(qaRequestId, session);
        return qaRequestId;
    }

    public QualityAssessmentResult getQualityAssessment(String qaRequestId, Duration timeout) throws Exception {
        QualityAssessmentSession session = activeSessions.get(qaRequestId);
        if (session != null) {
            return session.performAssessment();
        }
        throw new IllegalArgumentException("QA session not found: " + qaRequestId);
    }

    private static class QualityAssessmentSession {
        private final String qaRequestId;
        private final String workflowId;
        private final String executionId;
        private final Map<String, Object> productData;
        private final List<QualityCriterion> criteria;
        private final List<String> reviewers;

        public QualityAssessmentSession(String qaRequestId, String workflowId, String executionId,
                                      Map<String, Object> productData, List<QualityCriterion> criteria, List<String> reviewers) {
            this.qaRequestId = qaRequestId;
            this.workflowId = workflowId;
            this.executionId = executionId;
            this.productData = new HashMap<>(productData);
            this.criteria = new ArrayList<>(criteria);
            this.reviewers = new ArrayList<>(reviewers);
        }

        public QualityAssessmentResult performAssessment() {
            // Simulate QA assessment based on test results
            @SuppressWarnings("unchecked")
            Map<String, Integer> testResults = (Map<String, Integer>) productData.get("test_results");

            List<String> failedCriteria = new ArrayList<>();
            List<String> criticalIssues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            double totalScore = 0.0;
            int assessedCriteria = 0;

            for (QualityCriterion criterion : criteria) {
                String criterionName = criterion.getName();
                double threshold = criterion.getThreshold();

                // Map criteria to test results
                Integer testScore = switch (criterionName) {
                    case "functionality" -> testResults.get("functionality_tests");
                    case "performance" -> testResults.get("performance_tests");
                    case "security" -> testResults.get("security_tests");
                    case "usability" -> testResults.get("usability_tests");
                    default -> 90; // Default score
                };

                if (testScore != null) {
                    totalScore += testScore;
                    assessedCriteria++;

                    if (testScore < threshold) {
                        failedCriteria.add(criterionName + " (" + testScore + " < " + threshold + ")");

                        if (testScore < threshold - 10) {
                            criticalIssues.add("Critical issue in " + criterionName);
                        }

                        recommendations.add("Improve " + criterionName + " to meet " + threshold + "% threshold");
                    }
                }
            }

            double overallScore = assessedCriteria > 0 ? totalScore / assessedCriteria / 100.0 : 0.0;
            boolean passed = failedCriteria.isEmpty();

            return new QualityAssessmentResult(
                qaRequestId,
                passed,
                overallScore,
                failedCriteria,
                criticalIssues,
                recommendations
            );
        }
    }
}

class QualityCriterion {
    private final String name;
    private final String description;
    private final double threshold;

    public QualityCriterion(String name, String description, double threshold) {
        this.name = name;
        this.description = description;
        this.threshold = threshold;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getThreshold() { return threshold; }
}

class QualityAssessmentResult {
    private final String qaRequestId;
    private final boolean passed;
    private final double overallScore;
    private final List<String> failedCriteria;
    private final List<String> criticalIssues;
    private final List<String> recommendations;

    public QualityAssessmentResult(String qaRequestId, boolean passed, double overallScore,
                                 List<String> failedCriteria, List<String> criticalIssues, List<String> recommendations) {
        this.qaRequestId = qaRequestId;
        this.passed = passed;
        this.overallScore = overallScore;
        this.failedCriteria = new ArrayList<>(failedCriteria);
        this.criticalIssues = new ArrayList<>(criticalIssues);
        this.recommendations = new ArrayList<>(recommendations);
    }

    // Getters
    public String getQaRequestId() { return qaRequestId; }
    public boolean isPassed() { return passed; }
    public double getOverallScore() { return overallScore; }
    public List<String> getFailedCriteria() { return new ArrayList<>(failedCriteria); }
    public List<String> getCriticalIssues() { return new ArrayList<>(criticalIssues); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
}

// Additional supporting classes would be implemented similarly...

class MultiStageApprovalWorkflow {
    private final HumanInTheLoop hitlSystem;

    public MultiStageApprovalWorkflow(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public String startApprovalChain(String workflowId, String executionId, Map<String, Object> request, List<ApprovalStage> stages) {
        return "approval_chain_" + System.currentTimeMillis();
    }

    public ApprovalStageResult processApprovalStage(String chainId, int stageIndex, Duration timeout) {
        // Simulate approval processing
        return new ApprovalStageResult(ApprovalStageStatus.APPROVED, "mock_approver", "Approved by mock handler");
    }

    public ApprovalChainResult getFinalApprovalStatus(String chainId) {
        return new ApprovalChainResult(ApprovalChainStatus.APPROVED, List.of("stage1", "stage2", "stage3"), Duration.ofMillis(500));
    }
}

class ApprovalStage {
    private final String stageName;
    private final List<String> approvers;
    private final double thresholdAmount;

    public ApprovalStage(String stageName, List<String> approvers, double thresholdAmount) {
        this.stageName = stageName;
        this.approvers = new ArrayList<>(approvers);
        this.thresholdAmount = thresholdAmount;
    }

    public String getStageName() { return stageName; }
    public List<String> getApprovers() { return new ArrayList<>(approvers); }
    public double getThresholdAmount() { return thresholdAmount; }
}

class ApprovalStageResult {
    private final ApprovalStageStatus status;
    private final String approver;
    private final String comments;

    public ApprovalStageResult(ApprovalStageStatus status, String approver, String comments) {
        this.status = status;
        this.approver = approver;
        this.comments = comments;
    }

    public ApprovalStageStatus getStatus() { return status; }
    public String getApprover() { return approver; }
    public String getComments() { return comments; }
}

enum ApprovalStageStatus { PENDING, APPROVED, REJECTED, TIMEOUT }

class ApprovalChainResult {
    private final ApprovalChainStatus overallStatus;
    private final List<String> completedStages;
    private final Duration totalProcessingTime;

    public ApprovalChainResult(ApprovalChainStatus overallStatus, List<String> completedStages, Duration totalProcessingTime) {
        this.overallStatus = overallStatus;
        this.completedStages = new ArrayList<>(completedStages);
        this.totalProcessingTime = totalProcessingTime;
    }

    public ApprovalChainStatus getOverallStatus() { return overallStatus; }
    public List<String> getCompletedStages() { return new ArrayList<>(completedStages); }
    public Duration getTotalProcessingTime() { return totalProcessingTime; }
}

enum ApprovalChainStatus { PENDING, APPROVED, REJECTED, PARTIAL }

// Mock escalation handler
class MockEscalationHandler implements HumanInteractionHandler {

    @Override
    public CompletableFuture<HumanResponse> handleRequest(HumanRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(150 + new Random().nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (request instanceof EscalationRequest) {
                EscalationRequest escalation = (EscalationRequest) request;

                ResolutionAction action = switch (escalation.getEscalationType()) {
                    case SYSTEM_ERROR -> ResolutionAction.RETRY;
                    case BUSINESS_LOGIC -> ResolutionAction.MODIFY_AND_CONTINUE;
                    case DATA_QUALITY -> ResolutionAction.MANUAL_OVERRIDE;
                    default -> ResolutionAction.ESCALATE_FURTHER;
                };

                return new EscalationResponse(
                    request.getRequestId(),
                    "mock_escalation_handler",
                    "Issue resolved through mock escalation handling",
                    action,
                    Map.of("resolution_method", "automated_mock"),
                    false,
                    "Mock escalation handler resolved the issue"
                );
            }

            return HumanResponse.completed(request.getRequestId(), "mock_escalation_handler",
                                         Map.of("escalation_resolved", true), "Escalation resolved by mock handler");
        });
    }

    @Override
    public String getHandlerType() {
        return "mock_escalation";
    }

    @Override
    public boolean canHandle(HumanRequest request) {
        return request.getType() == RequestType.ESCALATION;
    }
}

// Simplified implementations of advanced pattern classes
class AdaptiveAutomationManager {
    private final HumanInTheLoop hitlSystem;
    private double automationRate = 0.2; // Start with 20% automation

    public AdaptiveAutomationManager(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public boolean shouldRequireHumanReview(Map<String, Object> taskData) {
        double confidence = (Double) taskData.get("confidence_score");
        return confidence < 0.8 || Math.random() > automationRate;
    }

    public void recordHumanFeedback(String taskId, boolean correct, double quality) {
        if (correct && quality > 0.9) {
            automationRate = Math.min(0.95, automationRate + 0.05);
        }
    }

    public void recordAutomatedResult(String taskId, double quality) {
        if (quality > 0.9) {
            automationRate = Math.min(0.95, automationRate + 0.02);
        }
    }

    public double getCurrentAutomationRate() { return automationRate; }
}

class ExpertiseRouter {
    private final HumanInTheLoop hitlSystem;
    private final Map<String, List<String>> expertSkills = new HashMap<>();

    public ExpertiseRouter(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public void registerExpert(String expertId, List<String> skills) {
        expertSkills.put(expertId, new ArrayList<>(skills));
    }

    public List<String> findExperts(List<String> requiredSkills) {
        return expertSkills.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(requiredSkills::contains))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
    }

    public String selectBestExpert(List<String> candidates, String requestType) {
        // Simple selection - first available
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}

class EscalationChainManager {
    private final HumanInTheLoop hitlSystem;

    public EscalationChainManager(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public String initiateEscalation(String issue, String workflowId, String executionId, String description, List<EscalationLevel> chain) {
        return "escalation_" + System.currentTimeMillis();
    }

    public boolean checkEscalationLevel(String escalationId, int level) {
        // Simulate resolution at different levels
        return Math.random() > 0.3; // 70% chance of resolution
    }
}

class EscalationLevel {
    private final String levelName;
    private final Duration timeout;
    private final List<String> responsiblePersons;

    public EscalationLevel(String levelName, Duration timeout, List<String> responsiblePersons) {
        this.levelName = levelName;
        this.timeout = timeout;
        this.responsiblePersons = new ArrayList<>(responsiblePersons);
    }

    public String getLevelName() { return levelName; }
    public Duration getTimeout() { return timeout; }
    public List<String> getResponsiblePersons() { return new ArrayList<>(responsiblePersons); }
}

class ConsensusManager {
    private final HumanInTheLoop hitlSystem;

    public ConsensusManager(HumanInTheLoop hitlSystem) {
        this.hitlSystem = hitlSystem;
    }

    public String startConsensusBuilding(String workflowId, String executionId, Map<String, Object> decisionContext, Duration timeout) {
        return "consensus_" + System.currentTimeMillis();
    }

    public ConsensusResult buildConsensus(String consensusId, Duration timeout) {
        // Simulate consensus building
        return new ConsensusResult(true, "Option B: Cloud Provider Y", 0.85, List.of());
    }
}

class ConsensusResult {
    private final boolean consensusAchieved;
    private final String selectedOption;
    private final double agreementLevel;
    private final List<String> dissentingOpinions;

    public ConsensusResult(boolean consensusAchieved, String selectedOption, double agreementLevel, List<String> dissentingOpinions) {
        this.consensusAchieved = consensusAchieved;
        this.selectedOption = selectedOption;
        this.agreementLevel = agreementLevel;
        this.dissentingOpinions = new ArrayList<>(dissentingOpinions);
    }

    public boolean isConsensusAchieved() { return consensusAchieved; }
    public String getSelectedOption() { return selectedOption; }
    public double getAgreementLevel() { return agreementLevel; }
    public List<String> getDissentingOpinions() { return new ArrayList<>(dissentingOpinions); }
}
