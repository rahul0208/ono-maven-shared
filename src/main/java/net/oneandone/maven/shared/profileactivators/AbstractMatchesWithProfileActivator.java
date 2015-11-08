package net.oneandone.maven.shared.profileactivators;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;

import java.util.Map;

public abstract class AbstractMatchesWithProfileActivator implements ProfileActivator {

    private final String prefix;

    public AbstractMatchesWithProfileActivator(String prefix) {
        this.prefix = "ono:" + prefix + ":";
    }

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        return new PA(profile, context, problems).invoke();
    }

    protected abstract boolean matches(String needle, String propertyToCheck);

    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        return false;
    }

    class PA {
        private Profile profile;
        private ProfileActivationContext context;
        private ModelProblemCollector problems;

        public PA(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
            this.profile = profile;
            this.context = context;
            this.problems = problems;
        }

        public boolean invoke() {
            final Activation activation = profile.getActivation();
            if (activation != null) {
                return withActivation(activation);
            } else {
                return false;
            }
        }

        private boolean withActivation(Activation activation) {
            final ActivationProperty property = activation.getProperty();
            if (property != null) {
                return withProperty(property);
            } else {
                return false;
            }
        }

        private boolean withProperty(ActivationProperty property) {
            final String name = property.getName();
            final String value = property.getValue();
            if (value != null && value.startsWith(prefix)) {
                return withValue(name, value);
            } else {
                return new PropertyProfileActivator().isActive(profile, context, problems);
            }
        }

        private boolean withValue(String name, String value) {
            final String needle = value.substring(prefix.length());
            final Map<String, String> systemProperties = context.getSystemProperties();
            final String propertyToCheck = systemProperties.get(name);
            return propertyToCheck != null && matches(needle, propertyToCheck);
        }
    }
}
