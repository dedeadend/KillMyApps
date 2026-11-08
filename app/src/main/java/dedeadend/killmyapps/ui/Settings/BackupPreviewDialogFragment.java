
package dedeadend.killmyapps.ui.Settings;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.databinding.DialogBackupPreviewBinding;
import dedeadend.killmyapps.util.BackupManager;

public class BackupPreviewDialogFragment extends DialogFragment {

    public static final String RESULT_KEY = "killmyapps_backup_restored";
    private static final String ARG_BACKUP_URI = "backup_uri";

    private BackupManager.BackupData backupData;
    private boolean loadingStarted;
    private boolean restoring;
    private int viewGeneration;
    private DialogBackupPreviewBinding binding;

    public static BackupPreviewDialogFragment newInstance(@NonNull Uri backupUri) {
        BackupPreviewDialogFragment fragment = new BackupPreviewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BACKUP_URI, backupUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogBackupPreviewBinding.inflate(getLayoutInflater());
        return new AlertDialog.Builder(requireContext())
                .setTitle("Restore backup")
                .setView(binding.getRoot())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss())
                .setPositiveButton("Restore", null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button restoreButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            restoreButton.setEnabled(backupData != null && !restoring);
            restoreButton.setOnClickListener(v -> restoreSelectedBackup());
        }
        if (!loadingStarted)
            loadSelectedBackup();
    }

    @Override
    public void onDestroyView() {
        viewGeneration++;
        loadingStarted = false;
        binding = null;
        super.onDestroyView();
    }

    private void loadSelectedBackup() {
        if (binding == null || loadingStarted)
            return;
        loadingStarted = true;
        showLoading("Reading and checking backup...");
        Bundle args = getArguments();
        String uriString = args != null ? args.getString(ARG_BACKUP_URI) : null;
        if (uriString == null) {
            showError("No backup file was selected.");
            return;
        }
        Uri backupUri = Uri.parse(uriString);
        Context appContext = requireContext().getApplicationContext();
        int requestGeneration = viewGeneration;
        App.executorService.execute(() -> {
            BackupManager.BackupData loadedBackup = null;
            String error = null;
            try {
                loadedBackup = BackupManager.readBackup(appContext, backupUri);
                if (loadedBackup == null)
                    error = "Could not read this backup file.";
            } catch (Exception e) {
                error = e.getMessage() == null
                        ? "Could not read this backup file."
                        : e.getMessage();
            }
            BackupManager.BackupData finalBackup = loadedBackup;
            String finalError = error;
            App.handler.post(() -> {
                if (!isAdded()
                        || binding == null
                        || getDialog() == null
                        || requestGeneration != viewGeneration)
                    return;
                if (finalError != null) {
                    showError(finalError);
                    return;
                }
                backupData = finalBackup;
                showBackupDetails(finalBackup);
                AlertDialog dialog = (AlertDialog) getDialog();
                if (dialog != null)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            });
        });
    }

    private void showBackupDetails(BackupManager.BackupData backup) {
        if (binding == null || backup == null)
            return;
        binding.backupPreviewProgress.setVisibility(View.GONE);
        binding.backupPreviewLoadingText.setVisibility(View.GONE);
        binding.backupPreviewError.setVisibility(View.GONE);
        binding.backupPreviewDetails.setVisibility(View.VISIBLE);
        binding.backupPreviewFileName.setText(backup.getFileName());
        binding.backupPreviewDate.setText(backup.getFormattedCreatedAt());
        binding.backupPreviewAppVersion.setText(backup.getAppVersion());
        binding.backupPreviewSelectionMode.setText(
                backup.getSelectionMode() == 0
                        ? "Exclude selected apps"
                        : "Include selected apps only"
        );
        binding.backupPreviewPackageCount.setText(String.valueOf(backup.getPackageCount()));
        binding.backupPreviewSettingsCount.setText(String.valueOf(backup.getSettingsCount()));
    }

    private void showLoading(String message) {
        if (binding == null)
            return;
        binding.backupPreviewProgress.setVisibility(View.VISIBLE);
        binding.backupPreviewLoadingText.setVisibility(View.VISIBLE);
        binding.backupPreviewLoadingText.setText(message);
        binding.backupPreviewError.setVisibility(View.GONE);
        binding.backupPreviewDetails.setVisibility(View.GONE);
    }

    private void showError(String message) {
        if (binding == null)
            return;
        binding.backupPreviewProgress.setVisibility(View.GONE);
        binding.backupPreviewLoadingText.setVisibility(View.GONE);
        binding.backupPreviewDetails.setVisibility(View.GONE);
        binding.backupPreviewError.setVisibility(View.VISIBLE);
        binding.backupPreviewError.setText(message);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void restoreSelectedBackup() {
        if (backupData == null || restoring || binding == null)
            return;

        restoring = true;
        setCancelable(false);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        }

        showLoading("Restoring Selection and Settings...");
        Context appContext = requireContext().getApplicationContext();
        BackupManager.BackupData selectedBackup = backupData;

        App.executorService.execute(() -> {
            String error = null;
            try {
                BackupManager.restoreBackup(appContext, selectedBackup);
            } catch (Exception e) {
                error = e.getMessage() == null
                        ? "Could not restore this backup."
                        : e.getMessage();
            }
            String finalError = error;
            App.handler.post(() -> {
                if (!isAdded() || binding == null || getDialog() == null)
                    return;
                if (finalError != null) {
                    restoring = false;
                    setCancelable(true);
                    showError(finalError);
                    AlertDialog currentDialog = (AlertDialog) getDialog();
                    if (currentDialog != null) {
                        currentDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setEnabled(backupData != null);
                        currentDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                .setEnabled(true);
                    }
                    return;
                }
                Bundle result = new Bundle();
                getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                Toast.makeText(appContext, "Backup restored successfully", Toast.LENGTH_LONG).show();
                dismiss();
            });
        });
    }
}
