package de.denniskniep.safed.common.verifications;

import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import de.denniskniep.safed.common.scans.AuthResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LineDiffVerification extends DiffVerification {

    @Override
    protected String getUnitName() {
        return "line";
    }

    @Override
    protected List<String> split(AuthResult authResult) {
        return Arrays.asList(authResult.extractVisibleText().split("\n"));
    }

    @Override
    protected String formatDiff(List<String> unitsA, Patch<String> patch) {
        var lines = UnifiedDiffUtils.generateUnifiedDiff(null, null, unitsA, patch, 3);
        // First two lines are "--- /dev/null" / "+++ /dev/null" file headers - meaningless without real file names.
        return String.join("\n", lines.subList(Math.min(2, lines.size()), lines.size()));
    }
}
