import SwiftUI
import shared

struct EditActivitiesSheet: View {

    @Binding var isPresented: Bool

    @EnvironmentObject private var nativeSheet: NativeSheet

    @State private var vm = EditActivitiesVm()

    var body: some View {

        VMView(vm: vm, stack: .ZStack(alignment: .bottomTrailing)) { state in

            List {
                ForEach(state.activitiesUI, id: \.activity.id) { activityUI in
                    ActivityItemView(vm: vm, activityUI: activityUI)
                }
            }

            HStack(alignment: .bottom) {

                Button(
                        action: {
                            nativeSheet.show { isAddActivityPresented in
                                ActivityFormSheet(
                                        isPresented: isAddActivityPresented,
                                        activity: nil
                                ) {}
                            }
                        },
                        label: {
                            Text("New Activity")
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                        }
                )
                        .background(roundedShape.fill(.blue))
                        .padding(.leading, 24)
                        .padding(.bottom, 8)
                        .safeAreaPadding(.bottom)

                Spacer()

                DialogCloseButton(isPresented: $isPresented, bgColor: c.bg)
            }
        }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .ignoresSafeArea()
    }

    private struct ActivityItemView: View {

        var vm: EditActivitiesVm
        var activityUI: EditActivitiesVm.ActivityUI

        @State private var isEditSheetPresented = false

        var body: some View {

            HStack(spacing: 8) {

                Text(activityUI.listText)
                        .lineLimit(1)

                Spacer()

                Button(
                        action: {
                            isEditSheetPresented = true
                        },
                        label: {
                            Image(systemName: "pencil")
                                    .font(.system(size: 16))
                                    .foregroundColor(.blue)
                        }
                )
                        .buttonStyle(.plain)
                        .padding(.leading, 8)
                        .sheetEnv(isPresented: $isEditSheetPresented) {
                            ActivityFormSheet(
                                    isPresented: $isEditSheetPresented,
                                    activity: activityUI.activity
                            ) {
                            }
                        }

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
