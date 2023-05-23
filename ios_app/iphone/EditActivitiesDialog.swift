import SwiftUI
import shared

struct EditActivitiesDialog: View {

    @Binding var isPresented: Bool

    @State private var vm = SortActivitiesVM()

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottomTrailing)) { state in

            List {
                ForEach(state.activitiesUI, id: \.activity.id) { activityUI in
                    ActivityItemView(vm: vm, activityUI: activityUI)
                }
            }

            DialogCloseButton(isPresented: $isPresented, bgColor: Color(.myBackground))
        }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .ignoresSafeArea()
    }

    private struct ActivityItemView: View {

        var vm: SortActivitiesVM
        var activityUI: SortActivitiesVM.ActivityUI

        var body: some View {

            HStack(spacing: 8) {

                Text(activityUI.listText)
                        .lineLimit(1)

                Spacer()

                Button(
                        action: {
                            vm.down(activityUI: activityUI)
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
                            vm.up(activityUI: activityUI)
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
