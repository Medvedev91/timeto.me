import SwiftUI
import shared

struct EditActivitiesDialog: View {

    @Binding var isPresented: Bool

    @EnvironmentObject var diApple: DIApple

    var body: some View {

        ZStack(alignment: .bottomTrailing) {

            List {
                ForEach(diApple.activities, id: \.id) { activity in
                    ActivityItemView(editActivitiesDialog: self, activity: activity)
                }
            }

            DialogCloseButton(isPresented: $isPresented, bgColor: Color(.myBackground))
        }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .ignoresSafeArea()
    }

    private struct ActivityItemView: View {

        var editActivitiesDialog: EditActivitiesDialog
        var activity: ActivityModel

        var body: some View {

            HStack(spacing: 8) {

                Text(activity.nameWithEmoji().removeTriggerIdsNoEnsure())
                        .lineLimit(1)

                Spacer(minLength: 0)

                Button(
                        action: {
                            var allActivities = editActivitiesDialog.diApple.activities.map {
                                $0
                            }
                            if allActivities.last == activity {
                                return
                            }
                            let oldIndex = allActivities.index(of: activity)!
                            allActivities.swapAt(oldIndex, oldIndex + 1)
                            allActivities.enumerated().forEach { newIndex, activity in
                                activity.upSort(newSort: newIndex.toInt32()) { _ in
                                    // todo
                                }
                            }
                        },
                        label: {
                            Image(systemName: "arrow.down")
                                    .font(.system(size: 14))
                                    .foregroundColor(.blue)
                        }
                )
                        .buttonStyle(.plain)
                        .padding(.leading, 8)

                Button(
                        action: {
                            var allActivities = editActivitiesDialog.diApple.activities.map {
                                $0
                            }
                            if allActivities.first == activity {
                                return
                            }
                            let oldIndex = allActivities.index(of: activity)!
                            allActivities.swapAt(oldIndex, oldIndex - 1)
                            allActivities.enumerated().forEach { newIndex, activity in
                                activity.upSort(newSort: newIndex.toInt32()) { _ in
                                    // todo
                                }
                            }
                        },
                        label: {
                            Image(systemName: "arrow.up")
                                    .font(.system(size: 14))
                                    .foregroundColor(.blue)
                        }
                )
                        .buttonStyle(.plain)
                        .padding(.leading, 6)
            }
        }
    }
}
