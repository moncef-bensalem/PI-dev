package test;

import database.EvaluationDAO;
import database.ScoreCompetenceDAO;
import model.Evaluation;
import model.ScoreCompetence;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("========== TEST EVALUATION ==========");
        EvaluationDAO evaluationDAO = new EvaluationDAO();
        System.out.println("\n--- 1. CREATE ---");
        Evaluation eval1 = new Evaluation(
                LocalDateTime.now(),
                "Nope",
                Evaluation.DecisionPreliminaire.DEFAVORABLE,
                3,
                3
        );
        evaluationDAO.add(eval1);
        System.out.println("Évaluation 1 ajoutée avec succès!");
        Evaluation eval2 = new Evaluation(
                LocalDateTime.now(),
                "Candidate needs improvement in communication skills",
                Evaluation.DecisionPreliminaire.A_REVOIR,
                3,
                3
        );
        evaluationDAO.add(eval2);
        System.out.println("Évaluation 2 ajoutée avec succès!");
        System.out.println("\n--- 2. READ (Toutes les évaluations) ---");
        List<Evaluation> evaluations = evaluationDAO.getAll();
        for (Evaluation e : evaluations) {
            System.out.println(e);
        }
        System.out.println("\n--- 3. READ (Évaluation spécifique) ---");
        if (!evaluations.isEmpty()) {
            Evaluation searchEval = new Evaluation();
            searchEval.setIdEvaluation(evaluations.get(0).getIdEvaluation());
            Evaluation foundEval = evaluationDAO.getOne(searchEval);
            if (foundEval != null) {
                System.out.println("Évaluation trouvée: " + foundEval);
            }
        }
        System.out.println("\n--- 4. UPDATE ---");
        if (!evaluations.isEmpty()) {
            Evaluation evalToUpdate = evaluations.get(0);
            System.out.println("Avant mise à jour: " + evalToUpdate);
            evalToUpdate.setCommentaireGlobal("Updated comment: Exceptional candidate!");
            evalToUpdate.setDecisionPreliminaire(Evaluation.DecisionPreliminaire.FAVORABLE);
            evaluationDAO.update(evalToUpdate);
            Evaluation updatedEval = evaluationDAO.getOne(evalToUpdate);
            System.out.println("Après mise à jour: " + updatedEval);
        }
        System.out.println("\n--- 5. DELETE ---");
        if (!evaluations.isEmpty()) {
            if (evaluations.size() > 1) {
                Evaluation evalToDelete = evaluations.get(1);
                System.out.println("Suppression de l'évaluation ID: " + evalToDelete.getIdEvaluation());
                evaluationDAO.delete(evalToDelete);
                System.out.println("Évaluation supprimée avec succès!");
            }
        }
        System.out.println("\n--- Vérification finale (après DELETE) ---");
        List<Evaluation> finalEvaluations = evaluationDAO.getAll();
        System.out.println("Nombre d'évaluations restantes: " + finalEvaluations.size());
        for (Evaluation e : finalEvaluations) {
            System.out.println(e);
        }
        System.out.println("\n========== TEST SCORE COMPETENCE ==========");
        ScoreCompetenceDAO scoreDAO = new ScoreCompetenceDAO();
        if (!finalEvaluations.isEmpty()) {
            int evaluationId = finalEvaluations.get(0).getIdEvaluation();
            System.out.println("\n--- 1. CREATE ---");
            ScoreCompetence score1 = new ScoreCompetence(
                    "Technical Skills",
                    18.5f,
                    "Very good understanding of Java and Spring Boot"
            );
            scoreDAO.addWithEvaluationId(score1, evaluationId);
            System.out.println("Score compétence 1 ajouté avec succès!");
            ScoreCompetence score2 = new ScoreCompetence(
                    "Communication",
                    15.0f,
                    "Good presentation skills, needs more confidence"
            );
            scoreDAO.addWithEvaluationId(score2, evaluationId);
            System.out.println("Score compétence 2 ajouté avec succès!");
            ScoreCompetence score3 = new ScoreCompetence(
                    "Problem Solving",
                    17.0f,
                    "Excellent analytical thinking"
            );
            scoreDAO.addWithEvaluationId(score3, evaluationId);
            System.out.println("Score compétence 3 ajouté avec succès!");
            System.out.println("\n--- 2. READ (Tous les scores) ---");
            List<ScoreCompetence> scores = scoreDAO.getAll();
            for (ScoreCompetence sc : scores) {
                System.out.println(sc);
            }
            System.out.println("\n--- 3. READ (Score spécifique) ---");
            if (!scores.isEmpty()) {
                ScoreCompetence searchScore = new ScoreCompetence();
                searchScore.setIdDetail(scores.get(0).getIdDetail());
                ScoreCompetence foundScore = scoreDAO.getOne(searchScore);
                if (foundScore != null) {
                    System.out.println("Score trouvé: " + foundScore);
                }
            }
            System.out.println("\n--- 4. UPDATE ---");
            if (!scores.isEmpty()) {
                ScoreCompetence scoreToUpdate = scores.get(0);
                System.out.println("Avant mise à jour: " + scoreToUpdate);
                scoreToUpdate.setNoteAttribuee(19.5f);
                scoreToUpdate.setAppreciationSpecifique("Exceptional technical skills, master of advanced concepts");
                scoreDAO.update(scoreToUpdate);
                ScoreCompetence updatedScore = scoreDAO.getOne(scoreToUpdate);
                System.out.println("Après mise à jour: " + updatedScore);
            }
            System.out.println("\n--- 5. DELETE ---");
            if (scores.size() > 2) {
                ScoreCompetence scoreToDelete = scores.get(2);
                System.out.println("Suppression du score ID: " + scoreToDelete.getIdDetail());
                scoreDAO.delete(scoreToDelete);
                System.out.println("Score supprimé avec succès!");
            }
            System.out.println("\n--- Vérification finale (après DELETE) ---");
            List<ScoreCompetence> finalScores = scoreDAO.getAll();
            System.out.println("Nombre de scores restants: " + finalScores.size());
            for (ScoreCompetence sc : finalScores) {
                System.out.println(sc);
            }
        } else {
            System.out.println("Aucune évaluation disponible pour tester ScoreCompetence!");
        }
    }
}